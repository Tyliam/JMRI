// ProgrammerConfigPane.java
package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ResourceBundle;
import javax.annotation.CheckForNull;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;

/**
 * Provide GUI to configure symbolic programmer defaults.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @version	$Revision$
 */
public class ProgrammerConfigPane extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = 3341676760826030384L;
    private final ResourceBundle apb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    public ProgrammerConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new java.awt.FlowLayout());
        p.add(new JLabel("Format:"));
        p.add(programmerBox = new JComboBox<>(ProgDefault.findListOfProgFiles()));
        programmerBox.setSelectedItem(ProgDefault.getDefaultProgFile());
        programmerBox.addActionListener((ActionEvent e) -> {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setDefaultFile(programmerBox.getSelectedItem().toString());
        });
        add(p);

        // also create the advanced panel
        advancedPanel = new JPanel();
        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
        advancedPanel.add(showEmptyTabs = new JCheckBox("Show empty tabs"));
        showEmptyTabs.setSelected(PaneProgFrame.getShowEmptyPanes());
        showEmptyTabs.addItemListener((ItemEvent e) -> {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowEmptyPanes(showEmptyTabs.isSelected());
        });
        advancedPanel.add(ShowCvNums = new JCheckBox("Show CV numbers in tool tips"));
        ShowCvNums.setSelected(PaneProgFrame.getShowCvNumbers());
        ShowCvNums.addItemListener((ItemEvent e) -> {
            InstanceManager.getDefault(ProgrammerConfigManager.class).setShowCvNumbers(ShowCvNums.isSelected());
        });
        this.add(advancedPanel);
        this.add(Box.createVerticalGlue());
    }
    JComboBox<String> programmerBox;

    /**
     * This constructor does nothing different than the default constructor.
     *
     * @param include ignored
     * @deprecated since 3.9.5
     */
    @Deprecated
    public ProgrammerConfigPane(boolean include) {
        this();
    }

    @CheckForNull
    public String getSelectedItem() {
        return (String) programmerBox.getSelectedItem();
    }

    public JPanel getAdvancedPanel() {
        return advancedPanel;
    }

    JPanel advancedPanel;
    JCheckBox showEmptyTabs;
    JCheckBox ShowCvNums;

    public boolean getShowEmptyTabs() {
        return showEmptyTabs.isSelected();
    }

    public boolean getShowCvNums() {
        return ShowCvNums.isSelected();
    }

    @Override
    public String getPreferencesItem() {
        return "ROSTER"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return this.apb.getString("MenuRoster"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return this.apb.getString("TabbedLayoutProgrammer"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return this.apb.getString("LabelTabbedLayoutProgrammer"); // NOI18N
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return true;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        InstanceManager.getDefault(ProgrammerConfigManager.class).savePreferences(ProfileManager.getDefault().getActiveProfile());
    }

    @Override
    public boolean isDirty() {
        String programmer = this.getSelectedItem();
        return (this.getShowEmptyTabs() != PaneProgFrame.getShowEmptyPanes()
                || this.getShowCvNums() != PaneProgFrame.getShowCvNumbers()
                || ((programmer != null)
                        ? !programmer.equals(ProgDefault.getDefaultProgFile())
                        : ProgDefault.getDefaultProgFile() != null));
    }

    @Override
    public boolean isRestartRequired() {
        return this.isDirty();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
