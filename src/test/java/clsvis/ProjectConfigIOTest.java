package clsvis;

import clsvis.model.ProjectConfig;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import org.junit.Test;


/**
 * Tests of {@link ProjectConfigIO}.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class ProjectConfigIOTest {

    @Test
    public void testSaveLoad1() {
        ProjectConfig expected = new ProjectConfig(
                new File( System.getProperty( "java.io.tmpdir" ), "test1.clsvis" ),
                Arrays.asList( new File("lib1.jar"), new File("lib2.jar") ),
                Arrays.asList( new File("part1.jar"), new File("dir1") )
        );
        ProjectConfigIO.save( expected );
        ProjectConfig actual = ProjectConfigIO.load( expected.path );
        assertEquals( expected, actual );
        System.out.println( actual );
    }

    @Test
    public void testSaveLoad2() {
        ProjectConfig expected = new ProjectConfig(
                new File( System.getProperty( "java.io.tmpdir" ), "test2.clsvis" ),
                Collections.emptyList(),
                Collections.emptyList()
        );
        ProjectConfigIO.save( expected );
        ProjectConfig actual = ProjectConfigIO.load( expected.path );
        assertEquals( expected, actual );
        System.out.println( actual );
    }
}
