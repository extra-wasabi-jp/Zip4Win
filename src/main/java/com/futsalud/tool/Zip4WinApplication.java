package com.futsalud.tool;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Zip4WinApplication implements ActionListener {

    private final String[] tableLabel = new String[]{"ファイルパス", "ファイル名", "サイズ", "タイムスタンプ"};

    private JFrame frame;
    private JPanel mainPane;
    private JPanel headPane;
    private JPanel bodyPane;
    private JPanel footPane;

    private JButton btnAddFiles;
    private JButton btnExecute;
    private JButton btnClear;
    private JButton btnQuit;

    private DefaultTableModel model;

    private JPasswordField txtPassword;
    private JPasswordField txtPasswordConfirm;


    private JTable table;

    private JFileChooser chooserInput;
    private JFileChooser chooserSave;

    private void execute() {

        mainPane = new JPanel(new BorderLayout());
        headPane = new JPanel(new BorderLayout());
        bodyPane = new JPanel(new BorderLayout());
        footPane = new JPanel();

        frame = new JFrame();

        txtPassword = new JPasswordField(12);
        txtPasswordConfirm = new JPasswordField(12);

        btnAddFiles = new JButton("追加");
        btnAddFiles.addActionListener(this);

        btnExecute = new JButton("圧縮");
        btnExecute.addActionListener(this);

        btnClear = new JButton("クリアー");
        btnClear.addActionListener(this);

        btnQuit = new JButton("終了");
        btnQuit.addActionListener(this);


        model = new DefaultTableModel(
                new String[][]{},
                tableLabel);

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        File defaultDir = new File(
                System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop");
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home"));
        }

        chooserInput = new JFileChooser();
        chooserInput.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile();
            }

            @Override
            public String getDescription() {
                return "* (すべてのファイル)";
            }
        });
        chooserInput.setMultiSelectionEnabled(true);
        chooserInput.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooserInput.setCurrentDirectory(defaultDir);

        chooserSave = new JFileChooser();
        chooserSave.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile() && f.getName().toLowerCase().endsWith(".zip");
            }

            @Override
            public String getDescription() {
                return "*.zip";
            }
        });
        chooserSave.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooserInput.setCurrentDirectory(defaultDir);

        JPanel headWestPane = new JPanel();
        JPanel headCenterPane = new JPanel();
        JPanel headEastPane = new JPanel();

        JPanel headCenterChild = new JPanel(new FlowLayout());
        headCenterChild.add(new JLabel("パスワード："));
        headCenterChild.add(txtPassword);
        headCenterChild.add(new JLabel("パスワード （確認）："));
        headCenterChild.add(txtPasswordConfirm);


        headEastPane.add(btnAddFiles);
        headEastPane.add(btnClear);
        headEastPane.add(btnExecute);
        headEastPane.add(btnQuit);
        headCenterPane.add(headCenterChild);

        headPane.add(headEastPane, BorderLayout.EAST);
        headPane.add(headCenterPane, BorderLayout.CENTER);
        headPane.add(headWestPane, BorderLayout.WEST);

        bodyPane.add(scrollPane, BorderLayout.CENTER);

        mainPane.add(headPane, BorderLayout.NORTH);
        mainPane.add(bodyPane, BorderLayout.CENTER);
        mainPane.add(footPane, BorderLayout.SOUTH);

        frame.setContentPane(mainPane);

        frame.setSize(new Dimension(1024, 768));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Zip4Win");
        frame.setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource().equals(btnQuit)) {
            frame.setVisible(false);
            System.exit(0);
        } else if (e.getSource().equals(btnAddFiles)) {
            int ret = chooserInput.showDialog(mainPane, "追加");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File[] files = chooserInput.getSelectedFiles();
                for (File file : files) {
                    model.addRow(fileToRow(file));
                }
            }
        } else if (e.getSource().equals(btnClear)) {
            model = new DefaultTableModel(new String[][]{}, tableLabel);
            table.setModel(model);
        } else if (e.getSource().equals(btnExecute)) {
            ImmutableList<String> errors = validator();
            if (errors.size() != 0) {
                JOptionPane.showMessageDialog(
                        mainPane,
                        errors.makeString("\n"),
                        "エラーメッセージ", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int ret = chooserSave.showDialog(mainPane, "保存");
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = chooserSave.getSelectedFile();
                if (file.exists()) {
                    ret = JOptionPane.showConfirmDialog(mainPane, "上書きか確認",
                            chooserSave.getSelectedFile().getName() + "は既に存在しています。上書きしてよろしいですか？",
                            JOptionPane.YES_NO_OPTION);
                    if (ret != JOptionPane.YES_OPTION) {
                        return;
                    }
                }
                MutableList<Zip4WinFileEntity> fileList = Lists.mutable.empty();
                for (int i = 0; i < model.getRowCount(); i++) {
                    fileList.add(new Zip4WinFileEntity(
                            model.getValueAt(i, 0).toString(),
                            model.getValueAt(i, 1).toString()));
                }
                String filename = file.getPath();
                if (!filename.endsWith(".zip")) {
                    filename = filename + ".zip";
                }
                ret = Zip4WinGenerator.generate(filename, txtPassword.getPassword(), fileList.toImmutable());
                if (ret != 0) {
                    JOptionPane.showMessageDialog(
                            mainPane, "エラーが発生しました", "システムエラー",
                            JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } else {
                    JOptionPane.showMessageDialog(
                            mainPane, "圧縮ファイルが作成されました", "完了メッセージ",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

    }

    private ImmutableList<String> validator() {

        MutableList<String> errors = Lists.mutable.empty();
        final String password = new String(txtPassword.getPassword());
        final String passwordConfirm = new String(txtPasswordConfirm.getPassword());

        if (!password.equals("")
            && !password.trim().equals(passwordConfirm)) {
            errors.add("パスワード（確認）が違います");
        }

        if (model.getRowCount() == 0) {
            errors.add("対象ファイルを追加してください");
        }
        return errors.toImmutable();
    }

    private String[] fileToRow(File file) {
        final String path = file.getParentFile().getPath();
        final String name = file.getName();
        final String size;
        if (file.length() > (1024 * 1024 * 1024)) {
            size = String.format("%1$.2fGB", BigDecimal.valueOf(file.length()).divide(
                    BigDecimal.valueOf(1024 * 1024 * 1024), 2).setScale(2, RoundingMode.HALF_UP));
        } else if (file.length() > 1024 * 1024) {
            size = String.format("%1$.2ffMB", BigDecimal.valueOf(file.length()).divide(
                    BigDecimal.valueOf(1024 * 1024), 2).setScale(2, RoundingMode.HALF_UP));
        } else if (file.length() > 1024) {
            size = String.format("%1$.2fKB", BigDecimal.valueOf(file.length()).divide(
                    BigDecimal.valueOf(1024), 2).setScale(2, RoundingMode.HALF_UP));
        } else {
            size = String.format("%dB", file.length());
        }
        final Date lastmodefied = new Date(file.lastModified());
        final LocalDateTime time = LocalDateTime.ofInstant(lastmodefied.toInstant(), ZoneId.systemDefault());
        final String date = time.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分ss秒"));

        return new String[]{path, name, size, date};
    }

    public static void main(String[] args) {
        Zip4WinApplication obj = new Zip4WinApplication();
        obj.execute();
    }

}
