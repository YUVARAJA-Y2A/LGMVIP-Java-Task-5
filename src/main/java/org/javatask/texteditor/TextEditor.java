package org.javatask.texteditor;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.undo.UndoManager;
import javax.swing.text.StyleConstants;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.ViewFactory;
import javax.swing.text.Element;
import javax.swing.text.AbstractDocument;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.IconView;
import javax.swing.text.ViewFactory;

import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import java.io.*;

public class TextEditor extends JFrame {

    private JTextPane textPane;
    private JFileChooser fileChooser;

    private UndoManager undoManager;

    public TextEditor() {
        setTitle("Text Editor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 14)); // Default font and size
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Initialize UndoManager
        undoManager = new UndoManager();
        Document doc = textPane.getDocument();
        doc.addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        });

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);



        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem closeItem = new JMenuItem("Close");
        JMenuItem printItem = new JMenuItem("Print");

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(closeItem);
        fileMenu.add(printItem);

        JMenu editMenu = new JMenu("Edit");
        menuBar.add(editMenu);

        JMenuItem cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        JMenuItem copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        JMenuItem pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        JMenuItem findItem = new JMenuItem("Find");

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(findItem);

        JMenu formatMenu = new JMenu("Format"); // New Format menu
        menuBar.add(formatMenu);

        JMenuItem boldItem = new JMenuItem("Bold");
        JMenuItem italicItem = new JMenuItem("Italic");
        JMenuItem underlineItem = new JMenuItem("Underline");
        JMenuItem colorItem = new JMenuItem("Color");

        formatMenu.add(boldItem);
        formatMenu.add(italicItem);
        formatMenu.add(underlineItem);
        formatMenu.add(colorItem);

        JMenu optionsMenu = new JMenu("Options"); // New Options menu
        menuBar.add(optionsMenu);

        JCheckBoxMenuItem wrapItem = new JCheckBoxMenuItem("Word Wrap");
        optionsMenu.add(wrapItem);


        JButton saveAndSubmitButton = new JButton("Save and Submit");
        saveAndSubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
                dispose(); // Close the text editor after saving
            }
        });
        add(saveAndSubmitButton, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();

        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveFile();
            }
        });

        closeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the text editor
            }
        });

        boldItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Bold, boldItem.isSelected());
            }
        });

        italicItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Italic, italicItem.isSelected());
            }
        });

        underlineItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Underline, underlineItem.isSelected());
            }
        });

        colorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseTextColor();
            }
        });

        wrapItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean wrapEnabled = wrapItem.isSelected();

                // Adjust the word wrap by changing the editor kit
                if (wrapEnabled) {
                    textPane.setEditorKit(new StyledEditorKit() {
                        @Override
                        public ViewFactory getViewFactory() {
                            return new WrapColumnFactory();
                        }
                    });
                } else {
                    textPane.setEditorKit(new StyledEditorKit());
                }
            }
        });


        printItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               printText();
            }
        });

        undoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    undo();
            }
        });

        redoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    redo();
            }
        });

        findItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = JOptionPane.showInputDialog(TextEditor.this, "Enter text to find:");
                if (searchText != null && !searchText.isEmpty()) {
                    String text = textPane.getText();
                    int index = text.indexOf(searchText);
                    if (index != -1) {
                        textPane.setSelectionStart(index);
                        textPane.setSelectionEnd(index + searchText.length());
                    } else {
                        JOptionPane.showMessageDialog(TextEditor.this, "Text not found!");
                    }
                }
            }
        });

        boldItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Bold, boldItem.isSelected());
            }
        });

        italicItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Italic, italicItem.isSelected());
            }
        });

        underlineItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyStyleToSelectedText(textPane, StyleConstants.Underline, underlineItem.isSelected());
            }
        });
        
    }


    // Custom ViewFactory for word wrap
    class WrapColumnFactory implements ViewFactory {
        @Override
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new WrapLabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new WrapParagraphView(elem);
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.Y_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }

            // default to text display
            return new WrapLabelView(elem);
        }
    }

    // Custom View for word wrap
    class WrapLabelView extends LabelView {
        public WrapLabelView(Element elem) {
            super(elem);
        }

        @Override
        public int getBreakWeight(int axis, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p0 = getStartOffset();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                if (p1 == p0) {
                    // Breaks at the first character (e.g., when the width is too narrow)
                    return BadBreakWeight;
                }
                return GoodBreakWeight;
            }
            return super.getBreakWeight(axis, pos, len);
        }

        @Override
        public View breakView(int axis, int p0, float pos, float len) {
            if (axis == View.X_AXIS) {
                checkPainter();
                int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                if (p0 == getStartOffset() && p1 == getEndOffset()) {
                    // Can't break; return null
                    return null;
                }
                return createFragment(p0, p1);
            }
            return super.breakView(axis, p0, pos, len);
        }
    }

    // Custom ParagraphView for word wrap
    class WrapParagraphView extends ParagraphView {
        public WrapParagraphView(Element elem) {
            super(elem);
        }

        @Override
        protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
            if (r == null) {
                r = new SizeRequirements();
            }
            float pref = layoutPool.getPreferredSpan(axis);
            float min = layoutPool.getMinimumSpan(axis);
            // Don't include insets, Box.getSpan will include them.
            r.minimum = (int) min;
            r.preferred = Math.max(r.minimum, (int) pref);
            r.maximum = Integer.MAX_VALUE;
            r.alignment = 0.5f;
            return r;
        }
    }

    private void undo() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
                updateTextPaneStyles();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void redo() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
                updateTextPaneStyles();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void openFile() {
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                textPane.setText(""); // Clear existing text
                while ((line = br.readLine()) != null) {
                    StyledDocument doc = textPane.getStyledDocument();
                    try {
                        doc.insertString(doc.getLength(), line + "\n", null);
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printText() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                textPane.printAll(graphics);

                return PAGE_EXISTS;
            }
        });

        if (printerJob.printDialog()) {
            try {
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateTextPaneStyles() {
        // Iterate through the document to update styles
        StyledDocument doc = textPane.getStyledDocument();
        Element root = doc.getDefaultRootElement();

        for (int i = 0; i < root.getElementCount(); i++) {
            Element paragraph = root.getElement(i);
            int start = paragraph.getStartOffset();
            int end = paragraph.getEndOffset() - 1; // Exclude newline character

            AttributeSet attrs = paragraph.getAttributes();

            // Check and apply styles (bold, italic, underline) for the paragraph
            applyStyleToParagraph(doc, start, end, attrs);
        }
    }

    private void applyStyleToParagraph(StyledDocument doc, int start, int end, AttributeSet attrs) {
        // Check and apply styles for the paragraph
        MutableAttributeSet set = new SimpleAttributeSet(attrs);

        // Example: Apply bold style
        if (StyleConstants.isBold(attrs)) {
            StyleConstants.setBold(set, true);
        } else {
            StyleConstants.setBold(set, false);
        }

        // Example: Apply italic style
        if (StyleConstants.isItalic(attrs)) {
            StyleConstants.setItalic(set, true);
        } else {
            StyleConstants.setItalic(set, false);
        }

        // Example: Apply underline style
        if (StyleConstants.isUnderline(attrs)) {
            StyleConstants.setUnderline(set, true);
        } else {
            StyleConstants.setUnderline(set, false);
        }

        // Apply the attributes to the paragraph
        doc.setCharacterAttributes(start, end - start, set, false);
    }
    private void applyStyleToSelectedText(JTextPane textPane, Object style, boolean value) {
        StyledDocument doc = textPane.getStyledDocument();
        int start = textPane.getSelectionStart();
        int end = textPane.getSelectionEnd();

        if (start != end) {
            MutableAttributeSet set;
            AttributeSet attrs = doc.getCharacterElement(start).getAttributes();

            if (attrs.containsAttribute(style, Boolean.TRUE)) {
                // If the style is already applied, use the existing attributes
                set = new SimpleAttributeSet(attrs);
                StyleConstants.setBold(set, false);
                StyleConstants.setItalic(set, false);
                StyleConstants.setUnderline(set, false);
            } else {
                // If the style is not applied, create a new set of attributes
                set = new SimpleAttributeSet();
            }

            // Apply the requested style
            if (style == StyleConstants.Bold) {
                StyleConstants.setBold(set, value);
            } else if (style == StyleConstants.Italic) {
                StyleConstants.setItalic(set, value);
            } else if (style == StyleConstants.Underline) {
                StyleConstants.setUnderline(set, value);
            }

            // Apply the attributes to the selected text
            doc.setCharacterAttributes(start, end - start, set, false);
        }
    }
    private void chooseTextColor() {
        Color color = JColorChooser.showDialog(this, "Choose Text Color", textPane.getForeground());
        if (color != null) {
            textPane.setForeground(color);
        }
    }
    private void saveFile() {
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(textPane.getText());
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TextEditor().setVisible(true);
            }
        });
    }
}
