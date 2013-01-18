package org.seventyeight.web.velocity;

import org.apache.velocity.VelocityContext;
import org.junit.Test;
import org.seventyeight.web.exceptions.TemplateDoesNotExistException;
import org.seventyeight.web.handler.TemplateManager;
import org.seventyeight.web.model.AbstractTheme;
import org.seventyeight.web.model.themes.Default;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author cwolfgang
 *         Date: 06-12-12
 *         Time: 21:53
 */
public class TemplateTest {
    @Test
    public void test01() throws TemplateDoesNotExistException {
        TemplateManager m = new TemplateManager();

        File template = new File( TemplateTest.class.getClassLoader().getResource( "velocity" ).getFile() );
        List<File> ts = new ArrayList<File>();
        ts.add( template );

        AbstractTheme theme = new Default();

        m.setTemplateDirectories( ts );
        m.initialize();

        VelocityContext vc = new VelocityContext();
        vc.put( "test", "1" );
        String result = m.getRenderer().setContext( vc ).setTheme( theme ).render( "test.vm" );

        assertThat( result, is( "TEST 1" ) );
    }
}
