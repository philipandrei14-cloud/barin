package com.baonbrain.ui;

import com.baonbrain.model.User;
import com.baonbrain.util.AppColors;
import com.baonbrain.util.DataStore;
import com.baonbrain.util.DataStore.Note;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class NotesPanel extends JPanel {

    // Fields
    private final User user;

    private DefaultListModel<String> noteListModel;
    private JList<String> noteList;
    private JTextArea noteEditor;
    private JTextField titleField;
    private JLabel statusLabel;

    private List<Note> notes = new ArrayList<>();
    private int selectedIndex = -1;

    //Constructor
    public NotesPanel(User user, MainFrame mainFrame) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(AppColors.BG_MAIN);
        initUI();
    }

    //UI
    private void initUI() {

        //Title Bar
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBar.setBackground(AppColors.BG_MAIN);
        titleBar.setBorder(new EmptyBorder(16, 20, 8, 20));
        JLabel pageTitle = new JLabel("📝 My Notes");
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        pageTitle.setForeground(AppColors.TEXT_DARK);
        titleBar.add(pageTitle);

        //Left Panel Note List
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setPreferredSize(new Dimension(230, 0));
        leftPanel.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, AppColors.BORDER));

        // Toolbar inside left panel
        JPanel listToolbar = new JPanel(new GridLayout(1, 2, 4, 0));
        listToolbar.setBackground(AppColors.PRIMARY);
        listToolbar.setBorder(new EmptyBorder(8, 8, 8, 8));
        JButton newBtn = makeBtn("+ New Note", AppColors.ACCENT);
        newBtn.addActionListener(e -> onNewNote());
        JButton delBtn = makeBtn("🗑 Delete", new Color(180, 40, 40));
        delBtn.addActionListener(e -> onDeleteNote());
        listToolbar.add(newBtn);
        listToolbar.add(delBtn);

        // Note list
        noteListModel = new DefaultListModel<>();
        noteList      = new JList<>(noteListModel);
        noteList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        noteList.setSelectionBackground(new Color(174, 214, 241));
        noteList.setFixedCellHeight(50);
        noteList.setCellRenderer(new NoteRenderer());
        noteList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onSelectNote();
        });
        JScrollPane listScroll = new JScrollPane(noteList);
        listScroll.setBorder(null);
        leftPanel.add(listToolbar, BorderLayout.NORTH);
        leftPanel.add(listScroll,  BorderLayout.CENTER);

        //Right Panel Editor
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(AppColors.BG_MAIN);
        rightPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Title input row
        JPanel titleRow = new JPanel(new BorderLayout(8, 0));
        titleRow.setBackground(AppColors.BG_MAIN);
        titleRow.setBorder(new EmptyBorder(0, 0, 8, 0));

        titleField = new JTextField("Select or create a note");
        titleField.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleField.setForeground(AppColors.TEXT_DARK);
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        titleField.setEnabled(false);

        JButton saveBtn = makeBtn("💾 Save", AppColors.ACCENT);
        saveBtn.setPreferredSize(new Dimension(100, 36));
        saveBtn.addActionListener(e -> onSaveNote());

        titleRow.add(titleField, BorderLayout.CENTER);
        titleRow.add(saveBtn,    BorderLayout.EAST);

        // Text area
        noteEditor = new JTextArea();
        noteEditor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        noteEditor.setLineWrap(true);
        noteEditor.setWrapStyleWord(true);
        noteEditor.setBorder(new EmptyBorder(10, 10, 10, 10));
        noteEditor.setEnabled(false);
        noteEditor.setForeground(AppColors.TEXT_MUTED);
        noteEditor.setText("👈 Select a note or click '+ New Note' to start writing.");

        JScrollPane editorScroll = new JScrollPane(noteEditor);
        editorScroll.setBorder(BorderFactory.createLineBorder(AppColors.BORDER));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        statusLabel.setForeground(AppColors.TEXT_MUTED);
        statusLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        rightPanel.add(titleRow,BorderLayout.NORTH);
        rightPanel.add(editorScroll,BorderLayout.CENTER);
        rightPanel.add(statusLabel,BorderLayout.SOUTH);

        //Split Pane
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,leftPanel,rightPanel);
        split.setDividerLocation(230);
        split.setDividerSize(1);
        split.setBorder(null);

        add(titleBar,BorderLayout.NORTH);
        add(split,BorderLayout.CENTER);
    }


    // ACTIONS all use DataStore, no file handling here
    //Called when user clicks "+ New Note"
    private void onNewNote() {
        Note note = new Note(0,
                "New Note " + (notes.size() + 1),
                "",
                new Date());

        DataStore.saveNote(user.getId(), note);
        refresh();

        // Select the newly created note
        noteList.setSelectedIndex(notes.size() - 1);
        enableEditor();
        titleField.setText(notes.get(notes.size() - 1).title);
        noteEditor.setText("");
        noteEditor.setForeground(AppColors.TEXT_DARK);
        titleField.selectAll();
        titleField.requestFocus();
        statusLabel.setText("✅ New note created.");
    }

    //Called when user selects a note from the list
    private void onSelectNote() {
        selectedIndex = noteList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= notes.size()) return;

        Note note = notes.get(selectedIndex);
        enableEditor();
        titleField.setText(note.title);
        noteEditor.setForeground(AppColors.TEXT_DARK);
        noteEditor.setText(note.content);
        noteEditor.setCaretPosition(0);
        statusLabel.setText("Last saved: " + formatDate(note.date));
    }

    //Called when user clicks "Save"
    private void onSaveNote() {
        if (selectedIndex < 0 || selectedIndex >= notes.size()) {
            statusLabel.setText("⚠️ No note selected.");
            return;
        }

        Note note    = notes.get(selectedIndex);
        note.title   = titleField.getText().trim().isEmpty()
                ? "Untitled" : titleField.getText().trim();
        note.content = noteEditor.getText();
        note.date    = new Date();

        DataStore.saveNote(user.getId(), note);
        refresh();
        noteList.setSelectedIndex(selectedIndex);
        statusLabel.setText("✅ Saved at " + formatDate(note.date));
    }

    // Called when user clicks "Delete"
    private void onDeleteNote() {
        if (selectedIndex < 0 || selectedIndex >= notes.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a note first.");
            return;
        }

        String name = notes.get(selectedIndex).title;
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + name + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (ok == JOptionPane.YES_OPTION) {
            DataStore.deleteNote(
                    user.getId(),
                    notes.get(selectedIndex).id);
            refresh();
            selectedIndex = -1;
            disableEditor();
            statusLabel.setText("🗑 Note deleted.");
        }
    }


    // REFRESH reload from DataStore
    public void refresh() {
        notes = DataStore.loadNotes(user.getId()); // ← DataStore loads it
        noteListModel.clear();
        for (Note note : notes) {
            noteListModel.addElement(note.title);
        }
    }


    // HELPERS
    private void enableEditor() {
        titleField.setEnabled(true);
        noteEditor.setEnabled(true);
    }

    private void disableEditor() {
        titleField.setText("Select or create a note");
        titleField.setEnabled(false);
        noteEditor.setText("👈 Select a note or click '+ New Note' to start writing.");
        noteEditor.setForeground(AppColors.TEXT_MUTED);
        noteEditor.setEnabled(false);
    }

    private String formatDate(Date d) {
        return new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(d);
    }

    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }


    // CUSTOM LIST RENDERER
    private class NoteRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JPanel cell = new JPanel(new BorderLayout(0, 3));
            cell.setBorder(new EmptyBorder(8, 12, 8, 8));

            JLabel titleLbl = new JLabel(value.toString());
            titleLbl.setFont(new Font("SansSerif", Font.BOLD, 13));

            Note note = (index < notes.size()) ? notes.get(index) : null;
            JLabel dateLbl = new JLabel(
                    note != null ? formatDate(note.date) : "");
            dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dateLbl.setForeground(AppColors.TEXT_MUTED);

            if (isSelected) {
                cell.setBackground(new Color(174, 214, 241));
                titleLbl.setForeground(AppColors.PRIMARY);
            } else {
                cell.setBackground(
                        index % 2 == 0 ? Color.WHITE
                                : new Color(248, 250, 252));
                titleLbl.setForeground(AppColors.TEXT_DARK);
            }

            cell.add(titleLbl, BorderLayout.CENTER);
            cell.add(dateLbl,  BorderLayout.SOUTH);
            return cell;
        }
    }
}