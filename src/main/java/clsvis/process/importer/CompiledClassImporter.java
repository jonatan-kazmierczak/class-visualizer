package clsvis.process.importer;

import clsvis.Utils;
import clsvis.model.Annotation_;
import clsvis.model.Class_;
import clsvis.model.ElementKind;
import clsvis.model.ElementModifier;
import clsvis.model.ElementVisibility;
import clsvis.model.Operation;
import clsvis.model.ParameterizableElement;
import clsvis.model.RelationDirection;
import clsvis.model.RelationType;
import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports class and its all membersMap from compiled code.
 * Before importClass(es) methods are invoked, class loader should be set by invocation of
 * {@link #setClassLoader(java.lang.ClassLoader)}.<br/>
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class CompiledClassImporter {

    /*
     * TODO:
     * - review of adding references - should be done only from importClass
     */

    /* CONSTANTS */
    private static final Logger logger = Logger.getLogger( CompiledClassImporter.class.getName() );

    private static final Set<ElementModifier> generalModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Public, ElementModifier.Protected, ElementModifier.Private,
            ElementModifier.Abstract, ElementModifier.Static, ElementModifier.Final,
            ElementModifier.Transient, ElementModifier.Volatile, ElementModifier.Synchronized,
            ElementModifier.Native, ElementModifier.Strict ) );
    private static final Set<ElementModifier> constantModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Static, ElementModifier.Final ) );
    private static final Set<ElementModifier> classModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Interface, ElementModifier.Enum, ElementModifier.Annotation,
            ElementModifier.LocalClass, ElementModifier.MemberClass,
            ElementModifier.Synthetic ) );
    public static final Set<ElementModifier> fieldModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Synthetic ) );
    public static final Set<ElementModifier> constructorModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Synthetic ) );
    private static final Set<ElementModifier> methodModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Synthetic, ElementModifier.Bridge, ElementModifier.Default ) );
    public static final Set<ElementModifier> parameterModifiers = Collections.unmodifiableSet( EnumSet.of(
            ElementModifier.Implicit, ElementModifier.Synthetic, ElementModifier.VarArgs ) );

    /** Map[ElementKind] = RelationType */
    private static final EnumMap<ElementKind, RelationType> memberKindRelations = new EnumMap<>( ElementKind.class );

    static {
        ElementKind[] memberKinds = { ElementKind.Constants, ElementKind.Fields, ElementKind.Properties,
            ElementKind.Constructors, ElementKind.Methods };
        RelationType[] relationTypes = { RelationType.Association, RelationType.Association, RelationType.Association,
            RelationType.Dependency, RelationType.Dependency };
        for (int i = 0; i < memberKinds.length; i++) {
            memberKindRelations.put( memberKinds[ i ], relationTypes[ i ] );
        }
    }

    /* FIELDS */
    // modifiers processors
    /** Map[ElementModifier] = Method */
    private final Map<ElementModifier, Method> generalModifierProcessors = new EnumMap<>( ElementModifier.class );
    /** Map[MemberClassName][ElementModifier] = Method */
    private final Map<String, Map<ElementModifier, Method>> elementModifierProcessors = new HashMap<>( 5, 1 );

    private final Map<String, Class_> importedClasses = new HashMap<>();
    private final Collection<String> notImportedClassNames = new HashSet<>();
    private URLClassLoader classLoader;

    private ImportProgressListener importProgressListener;

    /**
     * Initiates modifiers processors.
     */
    public CompiledClassImporter() {
        // Initialize generalModifierProcessors
        for (ElementModifier em : generalModifiers) {
            generalModifierProcessors.put( em, createMemberMethod( em.name(), Modifier.class, Integer.TYPE ) );
        }
        // Initialize elementModifierProcessors
        initModifierProcessors( classModifiers, Class.class );
        initModifierProcessors( fieldModifiers, Field.class );
        initModifierProcessors( constructorModifiers, Constructor.class );
        initModifierProcessors( methodModifiers, Method.class );
        initModifierProcessors( parameterModifiers, Parameter.class );
    }

    private void initModifierProcessors(Collection<ElementModifier> elementModifiers, Class clazz) {
        Map<ElementModifier, Method> modifierProcessors = new EnumMap<>( ElementModifier.class );
        elementModifierProcessors.put( clazz.getName(), modifierProcessors );
        for (ElementModifier em : elementModifiers) {
            modifierProcessors.put( em, createMemberMethod( em.name(), clazz ) );
        }
    }

    private static Method createMemberMethod(String suffix, Class memberClass, Class... paramTypes) {
        try {
            return memberClass.getMethod( "is" + suffix, paramTypes );
        } catch (Exception e) {
            throw new UnsupportedOperationException( "Expected method is not supported", e );
        }
    }

    /**
     * Imports the given classes.
     */
    public void importClasses(Collection<String> classNames) {
        int totalCount = classNames.size();
        int importedCount = 0;
        notImportedClassNames.clear();

        for (String className : classNames) {
            importClass( className );
            if (importProgressListener != null) {
                importProgressListener.importProgress( ++importedCount, totalCount );
            }
        }
    }

    /**
     * Imports the given class.
     */
    public void importClass(String className) {
        try {
            Class clazz = Class.forName( className, false, classLoader );
            importClass( clazz );
            // Exceptions from importClass don't stop the import process
        } catch (ClassNotFoundException e) {
            // Class added for loading cannot be load - serious damage
            processThrowable( Level.SEVERE, e, className );
            throw new ImportException( e );
        } catch (LinkageError e) {
            // Something wrong with loaded class - normal situation
            processThrowable( Level.WARNING, e, className );
            notImportedClassNames.add( className );
        } catch (RuntimeException e) {
            processThrowable( Level.WARNING, e, className );
            notImportedClassNames.add( className );
        } catch (Error e) {
            if (e instanceof OutOfMemoryError) {
                // Try to restore some memory
                importedClasses.clear();
                notImportedClassNames.clear();
                processThrowable( Level.SEVERE, e, className );
                System.exit( 1 );
            }
            processThrowable( Level.SEVERE, e, className );
            throw e;
        }
    }

    /**
     * Imports the given class.
     */
    public Class_ importClass(Class clazz) {
        Class_ class_ = importClassInternal( clazz );

        // Relations of class members
        for (Map.Entry<ElementKind, RelationType> memberKindRelation : memberKindRelations.entrySet()) {
            addRelations( class_, class_.getMembers( memberKindRelation.getKey() ), memberKindRelation.getValue() );
        }

        // Eventual parameterized types of class itself
        addRelations( class_, Arrays.asList( class_ ), RelationType.Dependency );

        // Discover relations of inner classes
        for (Class_ innerClass : class_.getRelations( RelationType.InnerClass, RelationDirection.Outbound )) {
            importClass( innerClass.originalTypeName );
        }

        // Cleanup of the class_
        class_.relationsFinished();

        return class_;
    }

    private Class_ importClassInternal(String className) {
        if (className == null) {
            return null;
        }
        Class_ cached = importedClasses.get( className );
        if (cached != null) {
            return cached;
        }
        try {
            Class clazz = Class.forName( className, false, classLoader );
            return importClassInternal( clazz );
        } catch (ClassNotFoundException e) {
            processThrowable( Level.SEVERE, e, className );
            throw new ImportException( e );
        }
    }

    /**
     * Invocation wrapper for {@link #importClassInternal0(java.lang.Class)}.
     */
    public Class_ importClassInternal(Class clazz) {
        clazz = Utils.getClassType( clazz );
        if (clazz == null) {
            return null;
        }
        // Check if already created
        String className = clazz.getName();
        Class_ cached = importedClasses.get( className );
        if (cached != null) {
            return cached;
        }

        try {
            return importClassInternal0( clazz );
        } catch (RuntimeException | Error e) {
            importedClasses.remove( className );
            throw e;
        }
    }

    private Class_ importClassInternal0(Class clazz) {
        // Process class
        Collection<Field> declaredFields = new LinkedHashSet<>( Arrays.asList( clazz.getDeclaredFields() ) );
        Collection<Method> declaredMethods = Arrays.asList( clazz.getDeclaredMethods() );
        Collection<ElementModifier> classModifiers = decodeModifiers( clazz.getModifiers(), clazz );
        String classCanonicalName = clazz.getCanonicalName();

        Class_ class_ = new Class_(
                classCanonicalName != null ? classCanonicalName : clazz.getName(),
                clazz.getSimpleName(),
                clazz,
                classModifiers,
                getKind( classModifiers, clazz ),
                getVisibility( classModifiers ) );
        importedClasses.put( clazz.getName(), class_ );

        // Store relation with super class
        Class superClass = clazz.getSuperclass();
        Type superGenericType = clazz.getGenericSuperclass();
        Class_ superClass_ = importClassInternal( superClass );
        if (superClass_ != null) {
            class_.addRelation( RelationType.SuperClass, superClass_ );
            class_.addMember(
                    new ParameterizableElement( "extends", "extends", superClass, superGenericType,
                            Collections.EMPTY_LIST, ElementKind.Extends, ElementVisibility.Local ) );
        }
        // Process eventual type params
        for (TypeVariable paramType : clazz.getTypeParameters()) {
            importTypeParameters( paramType, clazz, class_.typeParameters );
        }
        // Process parameterized super class as dependency
        importTypeParameters( superGenericType, superClass, class_.typeParameters );
        // Import super interfaces
        Class[] superInterfaces = clazz.getInterfaces();
        Type[] superGenericInterfaces = getCorrectedGenericTypes(
                clazz.getGenericInterfaces(), superInterfaces, class_.id, true );

        for (int i = 0; i < superGenericInterfaces.length; i++) {
            class_.addMember(
                    new ParameterizableElement(
                            "implements" + (i + 1), "implements" + (i + 1), superInterfaces[ i ], superGenericInterfaces[ i ],
                            Collections.EMPTY_LIST, ElementKind.Implements, ElementVisibility.Local ) );
        }

        for (Class usedSuperInterface : superInterfaces) {
            Class_ superInterface_ = importClassInternal( usedSuperInterface );
            class_.addSuperInterface( superInterface_ );
        }
        // Process parameterized interfaces as dependencies
        for (int i = 0; i < superGenericInterfaces.length; i++) {
            importTypeParameters( superGenericInterfaces[ i ], superInterfaces[ i ], class_.typeParameters );
        }
        // Import inner classes - compositions
        for (Class innerClass : clazz.getDeclaredClasses()) {
            try {
                Class_ innerClass_ = importClassInternal( innerClass );
                // Prevent from nulls caused by anonymous classes
                if (innerClass_ != null) {
                    class_.addRelation( RelationType.InnerClass, innerClass_ );
                }
            } catch (Throwable t) {
                // Error during importing inner class doesn't affect parent
                processThrowable( Level.WARNING, t, innerClass.getName() );
            }
        }

        // Import remaining membersMap
        // TODO: do review of methods' params
        importMethods( clazz, declaredFields, declaredMethods, class_ );
        importFields( clazz, declaredFields, class_ );
        importConstructors( clazz, clazz.getDeclaredConstructors(), class_ );
        importAnnotations( clazz.getDeclaredAnnotations(), class_.annotations );

        // Additional cleanup
        class_.membersFinished();

        return class_;
    }

    /**
     * Imports fields and breaks them down into constants and attributes.
     */
    private void importFields(
            Class clazz,
            Collection<Field> fields,
            Class_ class_) {

        Set<ElementModifier> constantDesignator = constantModifiers;

        for (Field field : fields) {
            String name = field.getName();
            Collection<ElementModifier> elementModifiers = decodeModifiers( field.getModifiers(), field );
            ParameterizableElement attribute;

            // Check if it is constant
            if (elementModifiers.containsAll( constantDesignator )) {
                // Remove constant designator - it's a trash in that context
                elementModifiers.removeAll( constantDesignator );
                // Create constant
                attribute = new ParameterizableElement(
                        field.toString(), name, field.getType(), field.getGenericType(), elementModifiers,
                        ElementKind.Constants, getVisibility( elementModifiers ) );
            } else {
                // Create attribute
                // FIX: should use field.toGenericString() as id, but it fails sometimes
                // - i.e. on com.sun.tools.internal.xjc.api.impl.j2s.JAXBModelImpl
                attribute = new ParameterizableElement(
                        field.toString(), name, field.getType(), field.getGenericType(), elementModifiers,
                        ElementKind.Fields, getVisibility( elementModifiers ) );
            }

            class_.addMember( attribute );
            importAnnotations( field.getDeclaredAnnotations(), attribute.annotations );

            // Process parameterized types as associations
            importTypeParameters( field.getGenericType(), field.getType(), attribute.typeParameters );
        } //loop: fields
    }

    private static final Pattern getterPattern = Pattern.compile( "(get|is)(\\p{Upper}\\w*)" );

    /**
     * Imports methods and breaks them down into properties and operations.
     * This method modifies <code>fields</code> in-out parameter,
     * so should be invoked before
     * {@link #importFields(java.lang.Class, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection)}.
     */
    private void importMethods(
            Class clazz,
            Collection<Field> fields,
            Collection<Method> methods,
            Class_ class_) {

        HashSet<Method> methodsToIgnore = new HashSet<>();

        // Looking for properties
        for (Method method : methods) {
            String name = method.getName();
            Collection<ElementModifier> elementModifiers = decodeModifiers( method.getModifiers(), method );
            Collection<Annotation_> annotations = new LinkedHashSet<>();
            importAnnotations( method.getDeclaredAnnotations(), annotations );

            // Is it getter (public, non-static, without params) ?
            Matcher accessorMatcher = getterPattern.matcher( name );
            boolean getterFound
                    = accessorMatcher.matches()
                    && method.getParameterTypes().length == 0
                    && elementModifiers.contains( ElementModifier.Public )
                    && !elementModifiers.contains( ElementModifier.Static );
            if (getterFound) {
                // Property found
                // Look for eventual matching setter
                Set<ElementModifier> setterVisibility = EnumSet.copyOf( ElementModifier.visibilityModifiers );
                String propertyName = accessorMatcher.group( 2 );
                try {
                    Method setter = clazz.getDeclaredMethod( "set" + propertyName, method.getReturnType() );
                    Collection<ElementModifier> setterModifiers = decodeModifiers( setter.getModifiers(), setter );
                    boolean setterFound = !elementModifiers.contains( ElementModifier.Static );
                    if (setterFound) {
                        setterVisibility.retainAll( setterModifiers );
                        elementModifiers.addAll( setterModifiers );
                        importAnnotations( setter.getDeclaredAnnotations(), annotations );
                        methodsToIgnore.add( setter );
                    }
                } catch (NoSuchMethodException e) {
                    setterVisibility.clear();
                }

                // Is it read-only property?
                if (!setterVisibility.contains( ElementModifier.Public )) {
                    elementModifiers.add( ElementModifier.ReadOnly );
                }

                // Look for eventual matching attribute
                //TODO: maybe should be limited to private ones?
                String attributeName = Introspector.decapitalize( propertyName );
                try {
                    Field attribute = clazz.getDeclaredField( attributeName );
                    if (attribute.getType().equals( method.getReturnType() )) {
                        elementModifiers.addAll( decodeModifiers( attribute.getModifiers(), attribute ) );
                        importAnnotations( attribute.getDeclaredAnnotations(), annotations );
                        fields.remove( attribute );
                    }
                } catch (NoSuchFieldException | SecurityException ignore) {
                }

                // Remove visibility modifiers and mark it as public
                elementModifiers.removeAll( ElementModifier.visibilityModifiers );
                elementModifiers.add( ElementModifier.Public );

                // Create property
                ParameterizableElement property = new ParameterizableElement(
                        method.toGenericString(), attributeName, method.getReturnType(), method.getGenericReturnType(),
                        elementModifiers, ElementKind.Properties, getVisibility( elementModifiers ) );
                property.annotations.addAll( annotations );
                class_.addMember( property );

                // Remove getter from further processing
                methodsToIgnore.add( method );

                // Process parameterized types as associations
                importTypeParameters( method.getGenericReturnType(), method.getReturnType(), property.typeParameters );
            } // if getterFound
        } // loop1: method

        // Processing remaining operations
        for (Method method : methods) {
            if (methodsToIgnore.contains( method )) {
                continue;
            }
            importOperation(
                    method.toGenericString(),
                    method.getName(),
                    method.getReturnType(),
                    method.getGenericReturnType(),
                    decodeModifiers( method.getModifiers(), method ),
                    method,
                    ElementKind.Methods,
                    class_ );
        } // loop2: method

        methodsToIgnore.clear();
    }

    private void importConstructors(
            Class clazz,
            Constructor[] declaredConstructors,
            Class_ class_) {
        // Process constructors
        for (Constructor method : declaredConstructors) {
            importOperation(
                    method.toGenericString(),
                    clazz.getSimpleName(),
                    void.class,
                    void.class,
                    decodeModifiers( method.getModifiers(), method ),
                    method,
                    ElementKind.Constructors,
                    class_ );
        }
    }

    /**
     * Common function for import all operations (methods and constructors).
     */
    private void importOperation(
            String id,
            String methodName,
            Class methodType,
            Type methodGenericType,
            Collection<ElementModifier> modifiers,
            Executable method,
            ElementKind elementKind,
            Class_ class_) {
        // Process parameters
        Parameter[] methodParams = method.getParameters();
        ArrayList<ParameterizableElement> parameters = new ArrayList<>( methodParams.length );
        for (Parameter methodParam : methodParams) {
            ParameterizableElement parameter
                    = new ParameterizableElement(
                            methodParam.getName(),
                            methodParam.getName(), methodParam.getType(), methodParam.getParameterizedType(),
                            decodeModifiers( methodParam.getModifiers(), methodParam ),
                            ElementKind.Parameters, ElementVisibility.Local );
            // Protection from AIOOBE caused by wrong signatures
            importAnnotations( methodParam.getAnnotations(), parameter.annotations );
            parameters.add( parameter );

            // Process parameterized type as dependency
            importTypeParameters( methodParam.getParameterizedType(), methodParam.getType(), parameter.typeParameters );
        }
        // Process throws
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        ArrayList<ParameterizableElement> throwables = new ArrayList<>( exceptionTypes.length );
        for (int i = 0; i < exceptionTypes.length; i++) {
            throwables.add(
                    new ParameterizableElement(
                            String.format( "[%d] %s", (i + 1), exceptionTypes[ i ].getCanonicalName() ),
                            "e" + (i + 1), exceptionTypes[ i ], exceptionTypes[ i ], Collections.EMPTY_LIST,
                            ElementKind.Throws, ElementVisibility.Local ) );
        }
        // Create operation
        Operation operation = new Operation( id, methodName, methodType, methodGenericType, modifiers,
                elementKind, getVisibility( modifiers ),
                parameters.isEmpty() ? Collections.EMPTY_LIST : parameters,
                throwables.isEmpty() ? Collections.EMPTY_LIST : throwables );
        importAnnotations( method.getDeclaredAnnotations(), operation.annotations );
        class_.addMember( operation );

        // Process parameterized type as dependency
        importTypeParameters( methodGenericType, methodType, operation.typeParameters );
    }

    private void importAnnotations(Annotation[] declaredAnnotations, Collection<Annotation_> annotations) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            Class<? extends Annotation> type = declaredAnnotation.annotationType();
            Annotation_ annotation
                    = new Annotation_( declaredAnnotation.toString(), "@" + type.getSimpleName(), type );
            annotations.add( annotation );
        }
    }

    private static void importTypeParameters(
            Type declaredType, Class classExclusion, Collection<String> typeParameters) {
        if (declaredType instanceof Type && !(declaredType instanceof Class)) {
            importTypeParameters( declaredType, classExclusion, typeParameters, Collections.EMPTY_LIST );
        }
    }

    private static void importTypeParameters(
            Type declaredType, Class classExclusion, Collection<String> typeParameters, Collection<Type> history) {

        if (history.contains( declaredType )) {
            return;
        }

        ArrayList<Type> lHistory = new ArrayList<>( history );
        lHistory.add( declaredType );

        if (declaredType instanceof Class) {
            Class type = Utils.getClassType( (Class) declaredType );
            if (type == null) {
                return;
            }
            String typeName = type.getName();
            if (!classExclusion.getName().equals( typeName ) && !typeParameters.contains( typeName )) {
                typeParameters.add( typeName );
            }
        } else if (declaredType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) declaredType;
            importTypeParameters( parameterizedType.getRawType(), classExclusion, typeParameters, lHistory );
            for (Type actualTypeArg : parameterizedType.getActualTypeArguments()) {
                importTypeParameters( actualTypeArg, classExclusion, typeParameters, lHistory );
            }
        } else if (declaredType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) declaredType;
            // Take lower bounds if exist (upper=Object - ignored), upper bounds otherwise
            boolean lowerBoundsExist = wildcardType.getLowerBounds().length > 0;
            Type[] bounds = lowerBoundsExist
                    ? wildcardType.getLowerBounds() : wildcardType.getUpperBounds();
            for (Type actualTypeArg : bounds) {
                importTypeParameters( actualTypeArg, classExclusion, typeParameters, lHistory );
            }
        } else if (declaredType instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) declaredType;
            importTypeParameters( genericArrayType.getGenericComponentType(), classExclusion, typeParameters, lHistory );
        } else if (declaredType instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) declaredType;
            Type[] bounds = typeVariable.getBounds();
            for (Type actualTypeArg : bounds) {
                importTypeParameters( actualTypeArg, classExclusion, typeParameters, lHistory );
            }
        }
        // Other Type specializations don't contain info about real types - ignored
    }

    private static ElementKind getKind(Collection<ElementModifier> modifiers, Class clazz) {
        if (modifiers.contains( ElementModifier.Annotation )) {
            return ElementKind.AnnotationType;
        }
        if (modifiers.contains( ElementModifier.Interface )) {
            return ElementKind.Interface;
        }
        if (modifiers.contains( ElementModifier.Enum )) {
            return ElementKind.Enum;
        }
        // Check if throwable
        while (clazz.getSuperclass() != null && !Object.class.getName().equals( clazz.getSuperclass().getName() )) {
            clazz = clazz.getSuperclass();
        }
        if (Throwable.class.getName().equals( clazz.getName() )) {
            return ElementKind.Throwable;
        }
        return ElementKind.Class;
    }

    private static ElementVisibility getVisibility(Collection<ElementModifier> modifiers) {
        if (modifiers.contains( ElementModifier.Public )) {
            return ElementVisibility.Public;
        }
        if (modifiers.contains( ElementModifier.Protected )) {
            return ElementVisibility.Protected;
        }
        if (modifiers.contains( ElementModifier.Private )) {
            return ElementVisibility.Private;
        }
        return ElementVisibility.Package;
    }

    /**
     * Decodes modifiers coded in integer value.
     */
    private Collection<ElementModifier> decodeModifiers(
            Integer modifiers, Object element) {
        Collection<ElementModifier> elementModifiers = EnumSet.noneOf( ElementModifier.class );

        for (Map.Entry<ElementModifier, Method> e : generalModifierProcessors.entrySet()) {
            try {
                if (Boolean.TRUE.equals( e.getValue().invoke( null, modifiers ) )) {
                    elementModifiers.add( e.getKey() );
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                // Ignore
                logger.log( Level.WARNING, "Unexpected exception during checking modifiers", ex );
            }
        }

        for (Map.Entry<ElementModifier, Method> e : elementModifierProcessors.get( element.getClass().getName() ).entrySet()) {
            try {
                if (Boolean.TRUE.equals( e.getValue().invoke( element ) )) {
                    elementModifiers.add( e.getKey() );
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                // Ignore
                logger.log( Level.WARNING, "Unexpected exception during checking modifiers", ex );
            }
        }

        return elementModifiers;
    }

    private void addRelations(Class_ class_, Collection<? extends ParameterizableElement> elements, RelationType relType) {
        for (ParameterizableElement element : elements) {
            addRelation( class_, element.originalTypeName, relType );
            for (String typeName : element.typeParameters) {
                addRelation( class_, typeName, relType );
            }
            for (Annotation_ annotation_ : element.annotations) {
                addRelation( class_, annotation_.originalTypeName, RelationType.DependencyAnnotation );
            }
            if (element instanceof Operation) {
                Operation operation = (Operation) element;
                addRelations( class_, operation.parameters, RelationType.Dependency );
                addRelations( class_, operation.throwables, RelationType.DependencyThrows );
            }
            // Cleanup
            element.typeParameters.clear();
            element.typeParameters = Collections.EMPTY_LIST;
            if (element.annotations.isEmpty()) {
                element.annotations = Collections.EMPTY_LIST;
            }
        } //loop
    }

    private void addRelation(Class_ class_, String targetType, RelationType relType) {
        try {
            Class_ targetClass = importClassInternal( targetType );
            if (targetClass != null && targetClass != class_) {
                class_.addRelation( relType, targetClass );
            }
        } catch (Throwable t) {
            // Error during importing reference doesn't affect main class
            processThrowable( Level.WARNING, t, "Problem during retrieving relation: {0} -> {1}",
                    class_.originalTypeName, targetType );
        }
    }

    /**
     * Process given throwable in context of the given className:<br/>
     * removes class from {@link #importedClasses} and logs throwable.
     */
    private void processThrowable(Level level, Throwable throwable, String className) {
        importedClasses.remove( className );
        processThrowable( level, throwable, "Problem during importing class: {0}", className, null );
    }

    private void processThrowable(Level level, Throwable throwable, String msg, String mainClass, String targetClass) {
        if (targetClass != null) {
            importedClasses.remove( targetClass );
        }
        logger.log( level, msg + "\n\t" + Utils.rootCauseAsString( throwable ), new String[]{ mainClass, targetClass } );
        logger.throwing( "", "", throwable );
    }

    private static Type[] getCorrectedGenericTypes(
            Type[] genericTypes, Class[] regularTypes, String elementId, boolean reportIncorrectGenerics) {
        Type[] results = genericTypes;

        if (regularTypes.length != genericTypes.length) {
            results = regularTypes;
            if (reportIncorrectGenerics && logger.isLoggable( Level.FINER )) {
                logger.finer(
                        String.format( "Wrong number of generic types (%d instead of %d) on element '%s'",
                                genericTypes.length, regularTypes.length, elementId ) );
                if (logger.isLoggable( Level.FINEST )) {
                    logger.finest( "regular types:" );
                    for (Class c : regularTypes) {
                        logger.finest( c.toString() );
                    }
                    logger.finest( "generic types:" );
                    for (Type t : genericTypes) {
                        logger.finest( t.toString() );
                    }
                }
            }
        }
        return results;
    }

    /**
     * Returns all imported classes.
     */
    public Collection<Class_> getImportedClasses() {
        return importedClasses.values();
    }

    /**
     * Returns root of all imported classes tree - representation of Object class.
     */
    public Class_ getImportedClassesRoot() {
        return importedClasses.get( Object.class.getName() );
    }

    /**
     * Returns small class for presentation: {@link AccessibleObject} if loaded, or result of
     * {@link #getImportedClassesRoot()} otherwise.
     */
    public Class_ getImportedSimpleClass() {
        Class_ smallClass = importedClasses.get( AccessibleObject.class.getName() );
        return smallClass != null ? smallClass : getImportedClassesRoot();
    }

    /**
     * Returns number of classes which could not be imported (due to some problems).
     */
    public int getNotImportedClassesCount() {
        return notImportedClassNames.size();
    }

    /**
     * Returns classLoader currently used to load classes by importClass(es) methods.
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets classLoader which will be used to load classes by importClass(es) methods.
     */
    public void setClassLoader(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets importProgressListener to receive notifications about import progress.
     */
    public void setImportProgressListener(ImportProgressListener importProgressListener) {
        this.importProgressListener = importProgressListener;
    }
}
