package clsvis.gui;

import clsvis.gui.graph.GraphComponent;
import clsvis.gui.model.ClassPresentationWrapper;
import clsvis.gui.model.ClassesTableModel;
import clsvis.gui.renderer.ClassesTableCellRenderer;
import clsvis.gui.renderer.CustomTreeCellRenderer;
import clsvis.gui.worker.SaveProjectTask;
import clsvis.gui.worker.ClassProcessorTask;
import clsvis.logging.GUIHandler;
import clsvis.model.Class_;
import clsvis.model.ElementModifier;
import clsvis.model.ProjectConfig;
import clsvis.model.RelationDirection;
import clsvis.model.RelationType;
import clsvis.process.importer.BaseProjectImporter;
import clsvis.process.importer.CompiledClassImporter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * The application's main frame.
 * 
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class MainFrame extends JFrame {

    private final Logger logger = Logger.getLogger( getClass().getName() );

    /** Currently selected {@link Class_}. */
    private Object currentClass_;

    /** Currently viewed {@link Class_}. */
    private Object viewedClass_;

    /* History of browsed classes. */
    private static final boolean ADD_CLASS_TO_HISTORY = true;
    private static final boolean ADD_CLASS_TO_HISTORY_DISABLED = false;
    private ClassesTableModel historyTableModel = new ClassesTableModel();
    /** Pointer to the location of {@link #currentClass_} in the {@link #historyTableModel}. */
    private int currentClassHistoryIndex = -1;

    private final ClassesTableRowFilter classesTableRowFilter = new ClassesTableRowFilter();

    private ProjectConfig projectConfig = new ProjectConfig();

    private BaseProjectImporter projectImporter = new BaseProjectImporter();

    /* Fields related to JFileChooser. */
    private static final boolean MULTI_SELECTION_ENABLED = true;
    private static final boolean MULTI_SELECTION_DISABLED = false;
    private final FileFilter jarFileFilter = new FileNameExtensionFilter( "Java Archive (.jar)", "jar" );
    private final FileFilter classFileFilter = new FileNameExtensionFilter( "Java Class (.class)", "class" );
    private final FileFilter projectFileFilter = new FileNameExtensionFilter(
            ConstantValues.APPLICATION_TITLE + " project (." + ConstantValues.PROJECT_FILE_EXTENSION + ")",
            ConstantValues.PROJECT_FILE_EXTENSION );

    private static final Color TREE_SELECTION_BG_COLOR = new Color( 0x87CEFA ); // LightSkyBlue

    private static final int TAB_IDX_CLASS_HIERARCHY = 2;

    private static final int STATUS_CLEANUP_DELAY_MS = 15_000;
    private Timer statusCleanupTimer = new Timer( 0, null );

    private static final String ABOUT_TITLE = "About";
    private static final String ABOUT_MESSAGE
            = "<html><center>"
            + "<font size='+2'><b>" + ConstantValues.APPLICATION_TITLE + "</b></font><br>"
            + "<font size='+1'>" + MainFrame.class.getPackage().getSpecificationVersion() + "</font>"
            + "<br><br>"
            + "<table cellpadding=0>"
            + "<tr><td>Build:        <td>" + MainFrame.class.getPackage().getImplementationVersion()
            + "<tr><td>JVM name:     <td>" + System.getProperty( "java.vm.name" )
            + "<tr><td>Java version: <td>" + System.getProperty( "java.runtime.version" )
            + "<tr><td>Java home:    <td>" + System.getProperty( "java.home" )
            + "</table>"
            + "<br>"
            + "Author: Jonatan Kaźmierczak<br>"
            + "Jonatan (at) Son-of-God.info"
            + "<br><br>"
            ;


    public MainFrame() {
        preInitComponents();
        initComponents();
        postInitComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openFileChooser = new JFileChooser();
        mainPanel = new JPanel();
        mainVerticalSplitPane = new JSplitPane();
        topLeftSplitPane = new JSplitPane();
        navigationTabbedPane = new JTabbedPane();
        classesTablePanel = new JPanel();
        filterPanel = new JPanel();
        filterLabel = new JLabel();
        classesTableFilterTextField = new JTextField();
        classesTableScrollPane = new JScrollPane();
        classesTable = new JTable();
        membersTreeScrollPane = new JScrollPane();
        membersTree = new JTree();
        classesTreeScrollPane = new JScrollPane();
        classesTree = new JTree();
        historyScrollPane = new JScrollPane();
        historyTable = new JTable();
        topRightSplitPane = new JSplitPane();
        centerTabbedPane = new JTabbedPane();
        graphScrollPane = new JScrollPane();
        rightTabbedPane = new JTabbedPane();
        membersEditorScrollPane = new JScrollPane();
        membersEditorPane = new JEditorPane();
        bottomPanel = new JPanel();
        consoleTitle = new JLabel();
        consoleScrollPane = new JScrollPane();
        consoleTextArea = new JTextArea();
        statusPanel = new JPanel();
        statusSeparator = new JSeparator();
        statusMessageLabel = new JLabel();
        progressBar = new JProgressBar();
        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        newProjectMenuItem = new JMenuItem();
        openProjectMenuItem = new JMenuItem();
        saveProjectMenuItem = new JMenuItem();
        saveProjectAsMenuItem = new JMenuItem();
        reloadProjectMenuItem = new JMenuItem();
        fileMenuSeparator1 = new JPopupMenu.Separator();
        addMenu = new JMenu();
        addJarsToClasspathMenuItem = new JMenuItem();
        importMenu = new JMenu();
        importJarMenuItem = new JMenuItem();
        importDirMenuItem = new JMenuItem();
        fileMenuSeparator2 = new JPopupMenu.Separator();
        exitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        copyMenuItem = new JMenuItem();
        navigationMenu = new JMenu();
        backMenuItem = new JMenuItem();
        forwardMenuItem = new JMenuItem();
        locateClassInHierarchyMenuItem = new JMenuItem();
        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();

        openFileChooser.setFileHidingEnabled(false);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Class Visualizer");

        mainPanel.setLayout(new BorderLayout());

        mainVerticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        mainVerticalSplitPane.setOneTouchExpandable(true);

        topLeftSplitPane.setDividerLocation(320);
        topLeftSplitPane.setOneTouchExpandable(true);

        classesTablePanel.setLayout(new BorderLayout());

        filterPanel.setLayout(new BorderLayout());

        filterLabel.setText(" Classes Filter ");
        filterPanel.add(filterLabel, BorderLayout.LINE_START);
        filterPanel.add(classesTableFilterTextField, BorderLayout.CENTER);

        classesTablePanel.add(filterPanel, BorderLayout.PAGE_START);

        classesTable.setAutoCreateRowSorter(true);
        classesTable.setModel(new ClassesTableModel(Collections.emptyList()));
        classesTable.setCellSelectionEnabled(true);
        classesTableScrollPane.setViewportView(classesTable);

        classesTablePanel.add(classesTableScrollPane, BorderLayout.CENTER);

        navigationTabbedPane.addTab("List", null, classesTablePanel, "<html>Select class to see its preview.<br>Double click on class name to navigate into it.<br>Click on header to change sort order.");

        membersTree.setCellRenderer(new CustomTreeCellRenderer());
        membersTree.setToggleClickCount(0);
        membersTreeScrollPane.setViewportView(membersTree);

        navigationTabbedPane.addTab("Browser", null, membersTreeScrollPane, "<html>Select class to see its preview.<br>Double click on class name to navigate into it.<br>Right click on element to see context menu.");
        membersTreeScrollPane.getAccessibleContext().setAccessibleName("");

        classesTree.setCellRenderer(new CustomTreeCellRenderer());
        classesTree.setToggleClickCount(0);
        classesTreeScrollPane.setViewportView(classesTree);

        navigationTabbedPane.addTab("Hierarchy", null, classesTreeScrollPane, "<html>Select class to see its preview.<br>Double click on class name to navigate into it.<br>Right click on element to see context menu.");

        historyTable.setAutoCreateRowSorter(true);
        historyTable.setModel(new ClassesTableModel());
        historyTable.setCellSelectionEnabled(true);
        historyScrollPane.setViewportView(historyTable);

        navigationTabbedPane.addTab("History", null, historyScrollPane, "<html>Select class to see its preview.<br>Double click on class name to navigate into it.<br>Click on header to change sort order.");

        topLeftSplitPane.setLeftComponent(navigationTabbedPane);

        topRightSplitPane.setOneTouchExpandable(true);

        centerTabbedPane.addTab("Relations Diagram", null, graphScrollPane, "<html>Click on class name to see its content.<br>Double click on class name to navigate into it.");

        topRightSplitPane.setLeftComponent(centerTabbedPane);

        membersEditorPane.setEditable(false);
        membersEditorPane.setContentType("text/html"); // NOI18N
        membersEditorPane.setDoubleBuffered(true);
        membersEditorScrollPane.setViewportView(membersEditorPane);

        rightTabbedPane.addTab("Preview", null, membersEditorScrollPane, "<html>Right click on class to locate it in Hierarchy.<br>Use mouse or press <code>Ctrl+A</code> to select elements.<br>Press <code>Ctrl+C</code> to copy selected text.");

        topRightSplitPane.setRightComponent(rightTabbedPane);

        topLeftSplitPane.setRightComponent(topRightSplitPane);

        mainVerticalSplitPane.setTopComponent(topLeftSplitPane);

        bottomPanel.setMinimumSize(new Dimension(0, 0));
        bottomPanel.setLayout(new BorderLayout());

        consoleTitle.setText(" Log");
        bottomPanel.add(consoleTitle, BorderLayout.PAGE_START);

        consoleTextArea.setEditable(false);
        consoleTextArea.setColumns(5);
        consoleTextArea.setRows(5);
        consoleScrollPane.setViewportView(consoleTextArea);

        bottomPanel.add(consoleScrollPane, BorderLayout.CENTER);

        mainVerticalSplitPane.setBottomComponent(bottomPanel);

        mainPanel.add(mainVerticalSplitPane, BorderLayout.CENTER);

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        statusPanel.setPreferredSize(new Dimension(400, 30));

        statusMessageLabel.setText("_");

        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(statusSeparator)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 565, Short.MAX_VALUE)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(statusPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(statusMessageLabel)
                    .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(statusPanel, BorderLayout.PAGE_END);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        newProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newProjectMenuItem.setMnemonic('n');
        newProjectMenuItem.setText("New Project");
        newProjectMenuItem.setToolTipText("Create new, empty project");
        newProjectMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(newProjectMenuItem);

        openProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openProjectMenuItem.setMnemonic('o');
        openProjectMenuItem.setText("Open Project");
        openProjectMenuItem.setToolTipText("Choose project file to be opened");
        openProjectMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                openProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openProjectMenuItem);

        saveProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveProjectMenuItem.setMnemonic('s');
        saveProjectMenuItem.setText("Save Project");
        saveProjectMenuItem.setToolTipText("Save current project");
        saveProjectMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveProjectMenuItem);

        saveProjectAsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        saveProjectAsMenuItem.setText("Save Project As ...");
        saveProjectAsMenuItem.setToolTipText("Choose location to save current project");
        saveProjectAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveProjectAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveProjectAsMenuItem);

        reloadProjectMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        reloadProjectMenuItem.setMnemonic('r');
        reloadProjectMenuItem.setText("Reload Current Project");
        reloadProjectMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                reloadProjectMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(reloadProjectMenuItem);
        fileMenu.add(fileMenuSeparator1);

        addMenu.setMnemonic('a');
        addMenu.setText("Add Required Libraries");
        addMenu.setToolTipText("<html>Add libraries required by classes you want to load.<br>You can point to some directory (i.e. Maven repository) and all jar files from all subdirectories will be added.");

        addJarsToClasspathMenuItem.setText("directories/JAR files...");
        addJarsToClasspathMenuItem.setToolTipText("Choose directories/JARs to add to class path");
        addJarsToClasspathMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addJarsToClasspathMenuItemActionPerformed(evt);
            }
        });
        addMenu.add(addJarsToClasspathMenuItem);

        fileMenu.add(addMenu);

        importMenu.setMnemonic('l');
        importMenu.setText("Load Classes");
        importMenu.setToolTipText("Load binary Java classes");

        importJarMenuItem.setText("from JAR files...");
        importJarMenuItem.setToolTipText("Choose JAR file(s) to load classes from");
        importJarMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importJarMenuItemActionPerformed(evt);
            }
        });
        importMenu.add(importJarMenuItem);

        importDirMenuItem.setText("from directory...");
        importDirMenuItem.setToolTipText("Choose class root directory to load classes from");
        importDirMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importDirMenuItemActionPerformed(evt);
            }
        });
        importMenu.add(importDirMenuItem);

        fileMenu.add(importMenu);
        fileMenu.add(fileMenuSeparator2);

        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.setToolTipText("Exit the application");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        copyMenuItem.setMnemonic('c');
        copyMenuItem.setText("Copy");
        copyMenuItem.setToolTipText("Copies text/diagram into clipboard");
        editMenu.add(copyMenuItem);

        menuBar.add(editMenu);

        navigationMenu.setMnemonic('n');
        navigationMenu.setText("Navigation");

        backMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));
        backMenuItem.setMnemonic('p');
        backMenuItem.setText("Back");
        backMenuItem.setToolTipText("Navigate to previous class from History");
        backMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                backMenuItemActionPerformed(evt);
            }
        });
        navigationMenu.add(backMenuItem);

        forwardMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));
        forwardMenuItem.setMnemonic('n');
        forwardMenuItem.setText("Forward");
        forwardMenuItem.setToolTipText("Navigate to next class from History");
        forwardMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                forwardMenuItemActionPerformed(evt);
            }
        });
        navigationMenu.add(forwardMenuItem);

        locateClassInHierarchyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        locateClassInHierarchyMenuItem.setMnemonic('l');
        locateClassInHierarchyMenuItem.setText("Locate Class in Hierarchy");
        locateClassInHierarchyMenuItem.setToolTipText("Locate in Hierarchy the class from current Preview");
        locateClassInHierarchyMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                locateClassInHierarchyMenuItemActionPerformed(evt);
            }
        });
        navigationMenu.add(locateClassInHierarchyMenuItem);

        menuBar.add(navigationMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit( 0 );
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void saveProjectAsMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveProjectAsMenuItemActionPerformed
        int choice = showFileChooser(
                FileChooserType.SAVE, projectFileFilter, FileChooserSelectionMode.FILES, MULTI_SELECTION_DISABLED,
                "Choose location to save current project" );
        if (choice == JFileChooser.APPROVE_OPTION) {
            executeWorker( new SaveProjectTask( this, openFileChooser.getSelectedFile() ) );
        }
    }//GEN-LAST:event_saveProjectAsMenuItemActionPerformed

    private void saveProjectMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveProjectMenuItemActionPerformed
        if (projectConfig.isPathSet()) executeWorker( new SaveProjectTask( this, projectConfig.path ) );
        else saveProjectAsMenuItemActionPerformed( evt );
    }//GEN-LAST:event_saveProjectMenuItemActionPerformed

    private void openProjectMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_openProjectMenuItemActionPerformed
        int choice = showFileChooser(
                FileChooserType.OPEN, projectFileFilter, FileChooserSelectionMode.FILES, MULTI_SELECTION_DISABLED,
                "Choose project file to be opened" );
        if (choice == JFileChooser.APPROVE_OPTION) {
            File selectedFile = openFileChooser.getSelectedFile();
            resetProject( selectedFile );
            executeWorker( new ClassProcessorTask( this, new ProjectConfig( selectedFile ) ) );
        }
    }//GEN-LAST:event_openProjectMenuItemActionPerformed

    private void newProjectMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_newProjectMenuItemActionPerformed
        resetProject( null );
        setTitle( ConstantValues.NEW_PROJECT_TITLE );
        String message = "New project created";
        setStatusMessage( message );
        logger.info( message );
    }//GEN-LAST:event_newProjectMenuItemActionPerformed

    private void importJarMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_importJarMenuItemActionPerformed
        int choice = showFileChooser(
                FileChooserType.OPEN, jarFileFilter, FileChooserSelectionMode.FILES, MULTI_SELECTION_ENABLED,
                "Choose JAR file(s) to load classes from" );
        if (choice == JFileChooser.APPROVE_OPTION) {
            executeWorker( new ClassProcessorTask( this,
                    new ProjectConfig( projectConfig.path, new File[0], openFileChooser.getSelectedFiles() ) ) );
        }
    }//GEN-LAST:event_importJarMenuItemActionPerformed

    private void importDirMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_importDirMenuItemActionPerformed
        int choice = showFileChooser(
                FileChooserType.OPEN, classFileFilter, FileChooserSelectionMode.DIRECTORIES, MULTI_SELECTION_DISABLED,
                "Choose class root directory to load classes from" );
        if (choice == JFileChooser.APPROVE_OPTION) {
            executeWorker( new ClassProcessorTask( this,
                    new ProjectConfig( projectConfig.path, new File[0], openFileChooser.getSelectedFile() ) ) );
        }
    }//GEN-LAST:event_importDirMenuItemActionPerformed

    private void addJarsToClasspathMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addJarsToClasspathMenuItemActionPerformed
        int choice = showFileChooser(
                FileChooserType.OPEN, jarFileFilter, FileChooserSelectionMode.FILES_AND_DIRECTORIES, MULTI_SELECTION_ENABLED,
                "Choose directories/JARs to add to class path" );
        if (choice == JFileChooser.APPROVE_OPTION) {
            executeWorker( new ClassProcessorTask( this,
                    new ProjectConfig( projectConfig.path, openFileChooser.getSelectedFiles(), new File[0] ) ) );
        }
    }//GEN-LAST:event_addJarsToClasspathMenuItemActionPerformed

    private void backMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_backMenuItemActionPerformed
        if (currentClassHistoryIndex > 0) {
            --currentClassHistoryIndex;
            selectClass( historyTableModel.getRow( currentClassHistoryIndex ), ADD_CLASS_TO_HISTORY_DISABLED );
        }
    }//GEN-LAST:event_backMenuItemActionPerformed

    private void forwardMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_forwardMenuItemActionPerformed
        if (currentClassHistoryIndex < historyTableModel.getRowCount() - 1) {
            ++currentClassHistoryIndex;
            selectClass( historyTableModel.getRow( currentClassHistoryIndex ), ADD_CLASS_TO_HISTORY_DISABLED );
        }
    }//GEN-LAST:event_forwardMenuItemActionPerformed

    private void locateClassInHierarchyMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_locateClassInHierarchyMenuItemActionPerformed
        navigationTabbedPane.setSelectedIndex( TAB_IDX_CLASS_HIERARCHY );
        selectClassOnClassesTree( viewedClass_ );
    }//GEN-LAST:event_locateClassInHierarchyMenuItemActionPerformed

    private void reloadProjectMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_reloadProjectMenuItemActionPerformed
        File projectPath = projectConfig.path;
        resetProject( projectPath );
        executeWorker( new ClassProcessorTask( this, new ProjectConfig( projectPath ) ) );
    }//GEN-LAST:event_reloadProjectMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(
                this,
                ABOUT_MESSAGE,
                ABOUT_TITLE,
                JOptionPane.PLAIN_MESSAGE
        );
    }//GEN-LAST:event_aboutMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JMenuItem aboutMenuItem;
    private JMenuItem addJarsToClasspathMenuItem;
    private JMenu addMenu;
    private JMenuItem backMenuItem;
    private JPanel bottomPanel;
    private JTabbedPane centerTabbedPane;
    private JTable classesTable;
    private JTextField classesTableFilterTextField;
    private JPanel classesTablePanel;
    private JScrollPane classesTableScrollPane;
    private JTree classesTree;
    private JScrollPane classesTreeScrollPane;
    private JScrollPane consoleScrollPane;
    private JTextArea consoleTextArea;
    private JLabel consoleTitle;
    private JMenuItem copyMenuItem;
    private JMenu editMenu;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JPopupMenu.Separator fileMenuSeparator1;
    private JPopupMenu.Separator fileMenuSeparator2;
    private JLabel filterLabel;
    private JPanel filterPanel;
    private JMenuItem forwardMenuItem;
    private JScrollPane graphScrollPane;
    private JMenu helpMenu;
    private JScrollPane historyScrollPane;
    private JTable historyTable;
    private JMenuItem importDirMenuItem;
    private JMenuItem importJarMenuItem;
    private JMenu importMenu;
    private JMenuItem locateClassInHierarchyMenuItem;
    private JPanel mainPanel;
    private JSplitPane mainVerticalSplitPane;
    private JEditorPane membersEditorPane;
    private JScrollPane membersEditorScrollPane;
    private JTree membersTree;
    private JScrollPane membersTreeScrollPane;
    private JMenuBar menuBar;
    private JMenu navigationMenu;
    private JTabbedPane navigationTabbedPane;
    private JMenuItem newProjectMenuItem;
    private JFileChooser openFileChooser;
    private JMenuItem openProjectMenuItem;
    private JProgressBar progressBar;
    private JMenuItem reloadProjectMenuItem;
    private JTabbedPane rightTabbedPane;
    private JMenuItem saveProjectAsMenuItem;
    private JMenuItem saveProjectMenuItem;
    private JLabel statusMessageLabel;
    private JPanel statusPanel;
    private JSeparator statusSeparator;
    private JSplitPane topLeftSplitPane;
    private JSplitPane topRightSplitPane;
    // End of variables declaration//GEN-END:variables

    private GraphComponent graphComponent;


    /**
     * Additional implemented initialization, before generated {@link #initComponents()}.
     */
    private void preInitComponents() {
        // Color re-definition for Nimbus LaF
        UIManager.put( "nimbusSelectionBackground", TREE_SELECTION_BG_COLOR );
        UIManager.put( "nimbusSelectedText", Color.BLACK );
    }

    /**
     * Additional implemented initialization, after generated {@link #initComponents()}.
     */
    private void postInitComponents() {
        stopProgress();

        // Logging
        GUIHandler.setTextArea( consoleTextArea );

        // Menu - actions and listener
        copyMenuItem.setActionCommand( (String) TransferHandler.getCopyAction().getValue( javax.swing.Action.NAME ) );
        copyMenuItem.addActionListener( new TransferActionListener() );

        // Graph
        graphComponent = new GraphComponent();
        graphComponent.setToolTipText( "" ); // Turn on tool tip functionality
        graphComponent.addMouseListener( new GraphMouseListener() );
        graphScrollPane.setViewportView( graphComponent );

        // Setup tables
        addTableSelectionListener( classesTable );
        addTableSelectionListener( historyTable );
        classesTable.addMouseListener( new ClassesTableMouseListener() );
        historyTable.addMouseListener( new ClassesTableMouseListener() );
        classesTable.setDefaultRenderer( String.class, new ClassesTableCellRenderer() );
        historyTable.setDefaultRenderer( String.class, new ClassesTableCellRenderer() );

        // Classes table filter
        classesTableFilterTextField.getDocument().addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                classesTableRowFilterUpdated();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                classesTableRowFilterUpdated();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                classesTableRowFilterUpdated();
            }
        } );

        // Setup trees
        addTreeSelectionListener( membersTree );
        addTreeSelectionListener( classesTree );
        membersTree.addMouseListener( new CustomTreeMouseListener() );
        classesTree.addMouseListener( new CustomTreeMouseListener() );

        // Adjust sizes of components
        mainVerticalSplitPane.setResizeWeight( 1.0 );
        topRightSplitPane.setResizeWeight( 0.7 );

        // Maximize window
        setExtendedState( MAXIMIZED_BOTH );

        newProjectMenuItemActionPerformed( null );
    }

    /**
     * Resets the state of the application - has to be done before new project is loaded.
     */
    private void resetProject(File path) {
        projectConfig = new ProjectConfig();
        projectConfig.path = path;
        projectImporter = new BaseProjectImporter();
        CompiledClassImporter classImporter = projectImporter.getClassImporter();
        classImporter.importClass( AccessibleObject.class );

        // Reset UI
        showClasses();
        classesTableFilterTextField.setText( "" );
        historyTableModel = new ClassesTableModel();
        historyTable.setModel( historyTableModel );
        currentClassHistoryIndex = -1;

        // Sweep all structures not used anymore
        System.gc();
    }

    /**
     * Shows imported classes.
     */
    public void showClasses() {
        getRootPane().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        CompiledClassImporter classImporter = projectImporter.getClassImporter();
        classesTable.setModel( new ClassesTableModel( classImporter.getImportedClasses() ) );
        TreeNode buildClassesTreeNode = StructureBuilder.buildClassesTreeNode2( classImporter.getImportedClassesRoot(), null );
        reloadTree( classesTree, buildClassesTreeNode );
        currentClass_ = null;
        viewedClass_ = null;
        selectClass( classImporter.getImportedSimpleClass(), ADD_CLASS_TO_HISTORY_DISABLED );
        getRootPane().setCursor( null );
    }

    /**
     * Process action: select class (anywhere).
     */
    void selectClass(Object selectedObject, boolean addToHistory) {
        if (selectedObject == null || !(selectedObject instanceof Class_) || currentClass_ == selectedObject) {
            return;
        }

        currentClass_ = selectedObject;
        Class_ class_ = (Class_) selectedObject;
        if (addToHistory) {
            ++currentClassHistoryIndex;
            historyTableModel.addRow( class_, currentClassHistoryIndex );
            historyTableModel.fireTableDataChanged();
        }

        // Graph
        graphComponent.setPreferredSize( graphScrollPane.getViewport().getSize() );
        graphComponent.setMainClass( class_ );

        // Members tree
        reloadTree( membersTree, StructureBuilder.buildMembersTreeNode( class_ ) );
        // Expand subnodes
        membersTree.expandRow( membersTree.getRowCount() - 1 ); // Relations
        membersTree.expandRow( 1 ); // Content

        viewClass( class_ );
    }

    /**
     * Show content of given class.
     */
    void viewClass(Object selectedObject) {
        if (selectedObject == null || !(selectedObject instanceof Class_) || viewedClass_ == selectedObject) {
            return;
        }
        viewedClass_ = selectedObject;
        membersEditorPane.setText( StructureBuilder.buildClassUMLTable( (Class_) selectedObject ) );
        membersEditorPane.setCaretPosition( 0 );
    }

    /**
     * Updates value of filter object and refreshes {@link #classesTable} accordingly.
     */
    void classesTableRowFilterUpdated() {
        classesTableRowFilter.filter = classesTableFilterTextField.getText();
        ((TableRowSorter) classesTable.getRowSorter()).setRowFilter( classesTableRowFilter );
    }

    private void addTableSelectionListener(JTable table) {
        table.getSelectionModel().addListSelectionListener( (ListSelectionEvent e) -> {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            int rowIdx = lsm.getLeadSelectionIndex();

            if (rowIdx > -1 && !e.getValueIsAdjusting()) {
                ClassesTableModel model = (ClassesTableModel) table.getModel();
                Class_ class_ = model.getRow( table.convertRowIndexToModel( rowIdx ) );
                viewClass( class_ );
            }
        } );
    }

    /**
     * Replaces root of the tree with the given rootNode.
     */
    private void reloadTree(JTree tree, TreeNode rootNode) {
        tree.setModel( new DefaultTreeModel( rootNode ) );
    }

    private void addTreeSelectionListener(JTree tree) {
        tree.addTreeSelectionListener( (TreeSelectionEvent e) -> {
            TreePath treePath = e.getNewLeadSelectionPath();

            if (treePath != null) {
                TreeNode clickedNode = (TreeNode) treePath.getLastPathComponent();
                Object userObject = clickedNode instanceof ClassPresentationWrapper
                        ? ((ClassPresentationWrapper) clickedNode).class_
                        : ((DefaultMutableTreeNode) clickedNode).getUserObject();
                viewClass( userObject );
            }
        } );
    }

    /**
     * Selects the given class on the {@link #classesTree}.
     */
    void selectClassOnClassesTree(Object userObject) {
        // build path from Class_es
        Class_ class_ = (Class_) userObject;
        // Only classes are handled
        if (class_.modifiers.contains( ElementModifier.Interface )) {
            return;
        }
        ArrayList<Class_> classPath = new ArrayList<>();
        do {
            classPath.add( class_ );
            if (class_.relationsMap.get( RelationDirection.Outbound ).get( RelationType.SuperClass ).isEmpty()) {
                break;
            }
            class_ = class_.relationsMap.get( RelationDirection.Outbound ).get( RelationType.SuperClass ).iterator().next();
        } while (true);
        Collections.reverse( classPath );
        // build equivalent path from TreeNodes
        ClassPresentationWrapper[] path = new ClassPresentationWrapper[classPath.size()];
        TreeModel treeModel = classesTree.getModel();
        ClassPresentationWrapper prevNode = (ClassPresentationWrapper) treeModel.getRoot();
        path[ 0 ] = prevNode;
        for (int i = 1; i < path.length; i++) {
            ClassPresentationWrapper currNode = new ClassPresentationWrapper( classPath.get( i ), null );
            currNode = (ClassPresentationWrapper) prevNode.getChildAt( prevNode.getIndex( currNode ) );
            path[ i ] = currNode;
            prevNode = currNode;
        }
        TreePath treePath = new TreePath( path );
        classesTree.setSelectionPath( treePath );
        classesTree.scrollPathToVisible( treePath );
    }

    private void executeWorker(SwingWorker<?, ?> worker) {
        worker.execute();
    }

    private int showFileChooser(
            FileChooserType dialogType,
            FileFilter fileFilter,
            FileChooserSelectionMode selectionMode,
            boolean multiSelectionEnabled,
            String title) {
        openFileChooser.setDialogType( dialogType.ordinal() );
        openFileChooser.setDialogTitle( title );
        openFileChooser.resetChoosableFileFilters();
        openFileChooser.setFileFilter( fileFilter );
        openFileChooser.setFileSelectionMode( selectionMode.ordinal() );
        openFileChooser.setMultiSelectionEnabled( multiSelectionEnabled );
        openFileChooser.setSelectedFile( null );
        openFileChooser.setSelectedFiles( null );

        return openFileChooser.showDialog( this, null );
    }

    public void startProgress() {
        progressBar.setVisible( true );
        progressBar.setIndeterminate( true );
    }

    public void moveProgress(int percent) {
        progressBar.setVisible( true );
        progressBar.setIndeterminate( false );
        progressBar.setValue( percent );
    }

    public void stopProgress() {
        progressBar.setVisible( false );
        progressBar.setValue( 0 );

        // Stop previous timer
        statusCleanupTimer.stop();
        // Clean status message after specified time
        statusCleanupTimer = new Timer(
                STATUS_CLEANUP_DELAY_MS,
                e -> setStatusMessage( "" )
        );
        statusCleanupTimer.start();
    }

    public void setStatusMessage(String message) {
        statusMessageLabel.setText( message );
    }

    @Override
    public void setTitle(String title) {
        super.setTitle( title + " - " + ConstantValues.APPLICATION_TITLE );
    }

    public ProjectConfig getProjectConfig() {
        return projectConfig;
    }

    public BaseProjectImporter getProjectImporter() {
        return projectImporter;
    }


    /**
     * Filters classes table by the given filter value (full class name containing filter).
     */
    private static class ClassesTableRowFilter extends RowFilter<ClassesTableModel, Integer> {
        private String filter = "";

        @Override
        public boolean include(Entry<? extends ClassesTableModel, ? extends Integer> entry) {
            return containsIgnoreCase(entry.getModel().getRow(entry.getIdentifier()).fullTypeName, filter);
        }

        private boolean containsIgnoreCase(String fullTypeName, String filter) {
            if (filter.isEmpty()) return true;
            if (fullTypeName.length() < filter.length()) return false;

            int j = 0; // filter index
            for (int i = 0; i < fullTypeName.length(); i++) {
                if (Character.toLowerCase(fullTypeName.charAt(i)) ==
                        Character.toLowerCase(filter.charAt(j))) {
                    j++;
                    if (j == filter.length()) return true;
                } else if (j > 0) {
                    // Backtrack: restart matching from current position
                    i = i - j; // will be incremented by loop
                    j = 0;
                }
            }
            return false;
        }
    } //class


    /**
     * Shows overview of selected class.
     */
    private class ClassesTableSelectionListener implements ListSelectionListener {
        private final JTable table;

        public ClassesTableSelectionListener(JTable table) {
            this.table = table;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            int rowIdx = lsm.getLeadSelectionIndex();

            if (rowIdx > -1 && !e.getValueIsAdjusting()) {
                ClassesTableModel model = (ClassesTableModel) table.getModel();
                Class_ class_ = model.getRow(table.convertRowIndexToModel(rowIdx));
                viewClass(class_);
            }
        }
    } //class


    /**
     * Handles double click on {@link Class_} table row.
     */
    private class ClassesTableMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent evt) {
            JTable table = (JTable) evt.getSource();
            int rowIdx = table.rowAtPoint( new Point( evt.getX(), evt.getY() ) );

            if (rowIdx > -1) {
                ClassesTableModel model = (ClassesTableModel) table.getModel();
                Class_ clickedObject = model.getRow( table.convertRowIndexToModel( rowIdx ) );
                if (evt.getClickCount() == 2) {
                    selectClass( clickedObject, ADD_CLASS_TO_HISTORY );
                }
            }
        }
    } //class


    /**
     * Handles double click on {@link Class_} tree node.
     */
    private class CustomTreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath treePath = ((JTree) e.getSource()).getPathForLocation( e.getX(), e.getY() );

            if (treePath != null) {
                TreeNode clickedNode = (TreeNode) treePath.getLastPathComponent();
                Object userObject = clickedNode instanceof ClassPresentationWrapper
                        ? ((ClassPresentationWrapper) clickedNode).class_
                        : ((DefaultMutableTreeNode) clickedNode).getUserObject();
                if (e.getClickCount() == 2) {
                    selectClass( userObject, ADD_CLASS_TO_HISTORY );
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //treeMousePressedOrReleased(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //treeMousePressedOrReleased(e);
        }
    } //class


    /**
     * Handles click on {@link Class_} graph vertex.
     */
    private class GraphMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent evt) {
            graphComponent.requestFocusInWindow();
            int clickCount = evt.getClickCount();
            Class_ clickedObject = graphComponent.selectVertexAt( evt.getX(), evt.getY() );
            if (clickCount == 2) {
                selectClass( clickedObject, ADD_CLASS_TO_HISTORY );
            } else if (clickCount == 1) {
                viewClass( clickedObject );
            }
        }
    } //class


    /**
     * Listens for and routes TransferActions.<br/>
     * Code of this class is based on the code of the class with the same name included in ListCutPaste example
     * provided by Sun/Oracle.
     */
    static class TransferActionListener implements ActionListener, PropertyChangeListener {
        private JComponent focusOwner;

        /** Registers the listener in KeyboardFocusManager. */
        public TransferActionListener() {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener( "permanentFocusOwner", this );
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (focusOwner != null) {
                String actionCommand = evt.getActionCommand();
                javax.swing.Action action = focusOwner.getActionMap().get( actionCommand );
                if (action != null) {
                    action.actionPerformed( new ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, null ) );
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Object newValue = evt.getNewValue();
            focusOwner = newValue instanceof JComponent ? (JComponent) newValue : null;
        }
    } //class


    private enum FileChooserType { OPEN, SAVE }

    private enum FileChooserSelectionMode { FILES, DIRECTORIES, FILES_AND_DIRECTORIES }
}
