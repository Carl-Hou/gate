/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.gate.gui.actions;

import com.mxgraph.util.mxResources;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateXMLUtils;
import org.gate.gui.GuiPackage;
import org.gate.gui.MainFrame;
import org.gate.gui.common.OptionPane;
import org.gate.gui.tree.action.ActionTree;
import org.gate.gui.tree.test.TestTree;
import org.gate.saveload.TreeModelSaver;
import org.gate.saveload.utils.DocumentHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Save extends AbstractGateAction {

    public static final String GMX_FILE_EXTENSION = ".gmx"; // $NON-NLS-1$
    private static final List<File> EMPTY_FILE_LIST = Collections.emptyList();

    // Whether we should keep backups for save JMX files. Default is to enable backup
    private static final boolean BACKUP_ENABLED =
            GateProps.getProperty("gate.gui.action.save.backup_on_save", true);
    // Path to the backup directory
    private static final String BACKUP_DIRECTORY =
            GateProps.getProperty("gate.gui.action.save.backup_directory", GateProps.getGateHome() + "/backups");

    // Backup files expiration in hours. Default is to never expire (zero value).
    private static final int BACKUP_MAX_HOURS =
            GateProps.getProperty("gate.gui.action.save.keep_backup_max_hours", 0);
    // Max number of backup files. Default is to limit to 10 backups max.
    private static final int BACKUP_MAX_COUNT =
            GateProps.getProperty("gate.gui.action.save.keep_backup_max_count", 10);
    // NumberFormat to format version number in backup file names
    private static final DecimalFormat BACKUP_VERSION_FORMATER = new DecimalFormat("000000"); //$NON-NLS-1$

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.SAVE);
        commands.add(ActionNames.SAVE_AS);
    }

    protected boolean showDialog = false;

    @Override
    public void doAction(ActionEvent e) throws IllegalUserActionException {
        File file = null;
        String defaultFileName = GuiPackage.getIns().getTestTree().getTestSuitesNode().getName() + ".gmx";
        if(e.getActionCommand().equals(ActionNames.SAVE)){
            if (GuiPackage.getIns().getMainFrame().getTestFile() == null) {
                file = selectedFile(defaultFileName);
            } else {
                file = new File(GuiPackage.getIns().getMainFrame().getTestFile());
            }
        } else if(e.getActionCommand().equals(ActionNames.SAVE_AS)){

            if(GuiPackage.getIns().getMainFrame().getTestFile()!= null){
                defaultFileName = GuiPackage.getIns().getMainFrame().getTestFile();
            }
            file = selectedFile(defaultFileName);
        }

        if(file == null){
            return;
        }

        if (file.exists()  &&
                JOptionPane.showConfirmDialog(null,mxResources.get("overwriteExistingFile")) != JOptionPane.YES_OPTION) {
            return;
        }
        log.info("Save to test file: " + file);
        // backup existing file according to jmeter/user.properties settings
        List<File> expiredBackupFiles = EMPTY_FILE_LIST;
        File fileToBackup = file;
        try {
            expiredBackupFiles = createBackupFile(fileToBackup);
        } catch (Exception ex) {
            log.error("Failed to create a backup for " + fileToBackup.getName(), ex); //$NON-NLS-1$
        }
        if(save(file)){
            GuiPackage.getIns().getMainFrame().setTestFile(file.getAbsolutePath());
            GuiPackage.getIns().getMainFrame().updateTitle();
        }

        for (File expiredBackupFile : expiredBackupFiles) {
            try {
                FileUtils.deleteQuietly(expiredBackupFile);
            } catch (Exception ex) {
                log.warn("Failed to delete backup file " + expiredBackupFile.getName()); //$NON-NLS-1$
            }
        }

    }
    File selectedFile(String defaultFileName){
        JFileChooser fileChooser = new JFileChooser(GateProps.getGateHome());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File(defaultFileName));
        // Adds the default file format
        if (fileChooser.showDialog(null, mxResources.get("save")) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return fileChooser.getSelectedFile();
    }
    boolean save(File file){
        TreeModelSaver testTreeSaver = new TreeModelSaver(GuiPackage.getIns().getTestTree().getClass().getSimpleName(), GuiPackage.getIns().getTestTree().getTestTreeModel());
        DocumentHelper testTreeDocumentHelper = null;
        TreeModelSaver actionTreeSaver = new TreeModelSaver(GuiPackage.getIns().getActionTree().getClass().getSimpleName(), GuiPackage.getIns().getActionTree().getTestTreeModel());
        DocumentHelper actionTreeDocumentHelper = null;

        DocumentHelper rootDocumentHelper = new DocumentHelper();
        rootDocumentHelper.createRootElement("GateModel");

        try {
            testTreeDocumentHelper = testTreeSaver.save();
            actionTreeDocumentHelper = actionTreeSaver.save();
            rootDocumentHelper.importNode(rootDocumentHelper.getRootElement(), testTreeDocumentHelper.getDocument().getDocumentElement());
            rootDocumentHelper.importNode(rootDocumentHelper.getRootElement(), actionTreeDocumentHelper.getDocument().getDocumentElement());
            GateXMLUtils.toFile(rootDocumentHelper.getDocument(), file);
        } catch (Throwable ex) {
            log.error("Fail to save :" + file.getAbsolutePath(), ex);
            OptionPane.showErrorMessageDialog("Error", ex);
            return false;
        }
        return true;
    }

    /**
     * <p>
     * Create a backup copy of the specified file whose name will be
     * <code>{baseName}-{version}.jmx</code><br>
     * Where :<br>
     * <code>{baseName}</code> is the name of the file to backup without its
     * <code>.jmx</code> extension. For a file named <code>testplan.jmx</code>
     * it would then be <code>testplan</code><br>
     * <code>{version}</code> is the version number automatically incremented
     * after the higher version number of pre-existing backup files. <br>
     * <br>
     * Example: <code>testplan-000028.jmx</code> <br>
     * <br>
     * If <code>jmeter.gui.action.save.backup_directory</code> is <b>not</b>
     * set, then backup files will be created in
     * <code>${JMETER_HOME}/backups</code>
     * </p>
     * <p>
     * Backup process is controlled by the following jmeter/user properties :<br>
     * <table border=1>
     * <tr>
     * <th align=left>Property</th>
     * <th align=left>Type/Value</th>
     * <th align=left>Description</th>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.backup_on_save</code></td>
     * <td><code>true|false</code></td>
     * <td>Enables / Disables backup</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.backup_directory</code></td>
     * <td><code>/path/to/backup/directory</code></td>
     * <td>Set the directory path where backups will be stored upon save. If not
     * set then backups will be created in <code>${JMETER_HOME}/backups</code><br>
     * If that directory does not exist, it will be created</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.keep_backup_max_hours</code></td>
     * <td><code>integer</code></td>
     * <td>Maximum number of hours to preserve backup files. Backup files whose
     * age exceeds that limit should be deleted and will be added to this method
     * returned list</td>
     * </tr>
     * <tr>
     * <td><code>jmeter.gui.action.save.keep_backup_max_count</code></td>
     * <td><code>integer</code></td>
     * <td>Max number of backup files to be preserved. Exceeding backup files
     * should be deleted and will be added to this method returned list. Only
     * the most recent files will be preserved.</td>
     * </tr>
     * </table>
     * </p>
     *
     * @param fileToBackup
     *            The file to create a backup from
     * @return A list of expired backup files selected according to the above
     *         properties and that should be deleted after the save operation
     *         has performed successfully
     */
    private List<File> createBackupFile(File fileToBackup) {
        if (!BACKUP_ENABLED || !fileToBackup.exists()) {
            return EMPTY_FILE_LIST;
        }
        char versionSeparator = '-'; //$NON-NLS-1$
        String baseName = fileToBackup.getName();
        // remove .jmx extension if any
        baseName = baseName.endsWith(GMX_FILE_EXTENSION) ? baseName.substring(0, baseName.length() - GMX_FILE_EXTENSION.length()) : baseName;
        // get a file to the backup directory
        File backupDir = new File(BACKUP_DIRECTORY);
        backupDir.mkdirs();
        if (!backupDir.isDirectory()) {
            log.error("Could not backup file ! Backup directory does not exist, is not a directory or could not be created ! <" + backupDir.getAbsolutePath() + ">"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // select files matching
        // {baseName}{versionSeparator}{version}{jmxExtension}
        // where {version} is a 6 digits number
        String backupPatternRegex = Pattern.quote(baseName + versionSeparator) + "([\\d]{6})" + Pattern.quote(GMX_FILE_EXTENSION); //$NON-NLS-1$
        Pattern backupPattern = Pattern.compile(backupPatternRegex);
        // create a file filter that select files matching a given regex pattern
        IOFileFilter patternFileFilter = new PrivatePatternFileFilter(backupPattern);
        // get all backup files in the backup directory
        List<File> backupFiles = new ArrayList<>(FileUtils.listFiles(backupDir, patternFileFilter, null));
        // find the highest version number among existing backup files (this
        // should be the more recent backup)
        int lastVersionNumber = 0;
        for (File backupFile : backupFiles) {
            Matcher matcher = backupPattern.matcher(backupFile.getName());
            if (matcher.find() && matcher.groupCount() > 0) {
                // parse version number from the backup file name
                // should never fail as it matches the regex
                int version = Integer.parseInt(matcher.group(1));
                lastVersionNumber = Math.max(lastVersionNumber, version);
            }
        }
        // find expired backup files
        List<File> expiredFiles = new ArrayList<>();
        if (BACKUP_MAX_HOURS > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, -BACKUP_MAX_HOURS);
            long expiryDate = cal.getTime().getTime();
            // select expired files that should be deleted
            IOFileFilter expiredFileFilter = FileFilterUtils.ageFileFilter(expiryDate, true);
            expiredFiles.addAll(FileFilterUtils.filterList(expiredFileFilter, backupFiles));
        }
        // sort backups from by their last modified time
        Collections.sort(backupFiles, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long diff = o1.lastModified() - o2.lastModified();
                // convert the long to an int in order to comply with the method
                // contract
                return diff < 0 ? -1 : diff > 0 ? 1 : 0;
            }
        });
        // backup name is of the form
        // {baseName}{versionSeparator}{version}{jmxExtension}
        String backupName = baseName + versionSeparator + BACKUP_VERSION_FORMATER.format(lastVersionNumber + 1) + GMX_FILE_EXTENSION;
        File backupFile = new File(backupDir, backupName);
        // create file backup
        try {
            FileUtils.copyFile(fileToBackup, backupFile);
        } catch (IOException e) {
            log.error("Failed to backup file :" + fileToBackup.getAbsolutePath(), e); //$NON-NLS-1$
            return EMPTY_FILE_LIST;
        }
        // add the fresh new backup file (list is still sorted here)
        backupFiles.add(backupFile);
        // unless max backups is not set, ensure that we don't keep more backups
        // than required
        if (BACKUP_MAX_COUNT > 0 && backupFiles.size() > BACKUP_MAX_COUNT) {
            // keep the most recent files in the limit of the specified max
            // count
            expiredFiles.addAll(backupFiles.subList(0, backupFiles.size() - BACKUP_MAX_COUNT));
        }
        return expiredFiles;
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    private static class PrivatePatternFileFilter implements IOFileFilter {

        private Pattern pattern;

        public PrivatePatternFileFilter(Pattern pattern) {
            if(pattern == null) {
                throw new IllegalArgumentException("pattern cannot be null !"); //$NON-NLS-1$
            }
            this.pattern = pattern;
        }

        @Override
        public boolean accept(File dir, String fileName) {
            return pattern.matcher(fileName).matches();
        }

        @Override
        public boolean accept(File file) {
            return accept(file.getParentFile(), file.getName());
        }
    }
}
