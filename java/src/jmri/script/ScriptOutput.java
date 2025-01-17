package jmri.script;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import javax.script.ScriptContext;
import javax.swing.JTextArea;
import jmri.InstanceManager;
import jmri.util.PipeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ScriptOutput {

    /**
     * JTextArea containing the output
     */
    private JTextArea output = null;
    private final static Logger log = LoggerFactory.getLogger(ScriptOutput.class);

    /**
     * Provide access to the JTextArea containing all ScriptEngine output.
     * <P>
     * The output JTextArea is not created until this is invoked, so that code
     * that doesn't use this feature can run on GUI-less machines.
     *
     * @return component containing script output
     */
    public JTextArea getOutputArea() {
        if (output == null) {
            // convert to stored output

            try {
                // create the output area
                output = new JTextArea();

                // Add the I/O pipes
                PipedWriter pw = new PipedWriter();

                ScriptContext context = JmriScriptEngineManager.getDefault().getDefaultContext();
                context.setErrorWriter(pw);
                context.setWriter(pw);

                // ensure the output pipe is read and stored into a
                // Swing TextArea data model
                PipedReader pr = new PipedReader(pw);
                PipeListener pl = new PipeListener(pr, output);
                pl.start();
            } catch (IOException e) {
                log.error("Exception creating script output area", e);
                return null;
            }
        }
        return output;
    }

    static public ScriptOutput getDefault() {
        if (InstanceManager.getDefault(ScriptOutput.class) == null) {
            InstanceManager.store(new ScriptOutput(), ScriptOutput.class);
        }
        return InstanceManager.getDefault(ScriptOutput.class);
    }
}
