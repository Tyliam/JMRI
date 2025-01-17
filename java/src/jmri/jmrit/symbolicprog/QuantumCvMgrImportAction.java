// QuantumCvMgrImportAction.java
package jmri.jmrit.symbolicprog;

import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to import the CV values from a Quantum CV Manager .qcv file.
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author	Dave Heap Copyright (C) 2015
 * @version $Revision: 22821 $
 */
public class QuantumCvMgrImportAction extends GenericImportAction {

    public QuantumCvMgrImportAction(String actionName, CvTableModel pModel, JFrame pParent, JLabel pStatus) {
        super(actionName, pModel, pParent, pStatus, "Quantum CV Manager files", "qcv", null);
    }

    @Override
    boolean launchImporter(File file, CvTableModel tableModel) {
            try {
                // ctor launches operation
                new QuantumCvMgrImporter(file, mModel);
                return true;
            } catch (IOException ex) {
                return false;
        }
    }

    static Logger log = LoggerFactory.getLogger(QuantumCvMgrImportAction.class.getName());
}
