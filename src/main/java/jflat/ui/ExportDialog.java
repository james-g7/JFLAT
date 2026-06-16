package ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import javax.imageio.ImageIO;

public class ExportDialog extends JDialog {

    // 1. Updated to accept any type of Automaton Canvas (FSA, Mealy, etc.)
    private final AbstractAutomatonCanvas<?, ?, ?> canvas;

    // UI Components we need to track
    private JRadioButton visRadio;
    private JRadioButton latexRadio;
    private JComboBox<String> formatComboBox;

    private JPanel previewContainer;
    private CardLayout previewCards;
    private JLabel imagePreviewLabel;
    private JTextArea textPreviewArea;

    private BufferedImage currentVisualBuffer;
    private String currentLatexBuffer;

    public ExportDialog(JFrame parent, AbstractAutomatonCanvas<?, ?, ?> canvas) {
        super(parent, "Export Automaton", true);
        this.canvas = canvas;

        setLayout(new BorderLayout());
        setSize(700, 500);
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        updatePreview(); // Generate initial preview

        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // --- 1. SETTINGS PANEL (Left Side) ---
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        settingsPanel.setPreferredSize(new Dimension(250, 0));

        JLabel typeLabel = new JLabel("Export Type:");
        typeLabel.setFont(typeLabel.getFont().deriveFont(Font.BOLD));

        visRadio = new JRadioButton("Visual Layout", true);
        latexRadio = new JRadioButton("LaTeX Code");

        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(visRadio);
        typeGroup.add(latexRadio);

        JLabel formatLabel = new JLabel("File Format:");
        formatLabel.setFont(formatLabel.getFont().deriveFont(Font.BOLD));

        // 2. Fixed dropdown to match supported ImageIO formats
        formatComboBox = new JComboBox<>(new String[]{"PNG Image (*.png)", "JPEG Image (*.jpg)"});

        // Add action listeners to swap formats based on radio button choice
        visRadio.addActionListener(e -> {
            formatComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"PNG Image (*.png)", "JPEG Image (*.jpg)"}));
            updatePreview();
        });
        latexRadio.addActionListener(e -> {
            // 3. Fixed dropdown to match supported Text formats
            formatComboBox.setModel(new DefaultComboBoxModel<>(new String[]{"LaTeX Source (*.tex)", "Plain Text (*.txt)"}));
            updatePreview();
        });

        // Add components to settings panel with spacing
        settingsPanel.add(typeLabel);
        settingsPanel.add(Box.createVerticalStrut(5));
        settingsPanel.add(visRadio);
        settingsPanel.add(latexRadio);
        settingsPanel.add(Box.createVerticalStrut(20));
        settingsPanel.add(formatLabel);
        settingsPanel.add(Box.createVerticalStrut(5));
        settingsPanel.add(formatComboBox);
        settingsPanel.add(Box.createVerticalGlue()); // Pushes everything to the top

        // --- 2. PREVIEW PANEL (Right Side) ---
        previewCards = new CardLayout();
        previewContainer = new JPanel(previewCards);
        previewContainer.setBorder(BorderFactory.createTitledBorder("Live Preview"));

        // Visual Preview Card
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScroll = new JScrollPane(imagePreviewLabel);
        imageScroll.setBorder(null);

        // Text Preview Card
        textPreviewArea = new JTextArea();
        textPreviewArea.setEditable(false);
        textPreviewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane textScroll = new JScrollPane(textPreviewArea);

        previewContainer.add(imageScroll, "VISUAL");
        previewContainer.add(textScroll, "TEXT");

        // Split Pane to hold both sides
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, settingsPanel, previewContainer);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);

        // --- 3. BUTTON PANEL (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        JButton exportBtn = new JButton("Export");

        cancelBtn.addActionListener(e -> dispose());
        exportBtn.addActionListener(e -> performExport());

        getRootPane().setDefaultButton(exportBtn);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(exportBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the right-side panel with either the image buffer or the LaTeX string.
     */
    private void updatePreview() {
        if (visRadio.isSelected()) {
            previewCards.show(previewContainer, "VISUAL");

            int width = Math.max(canvas.getWidth(), 800);
            int height = Math.max(canvas.getHeight(), 600);

            currentVisualBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = currentVisualBuffer.createGraphics();
            canvas.paintComponent(g2d); // Render canvas to buffer
            g2d.dispose();

            // Scale the preview so it fits nicely in the dialog window
            Image scaledPreview = currentVisualBuffer.getScaledInstance(
                    previewContainer.getWidth() > 0 ? previewContainer.getWidth() - 20 : 400,
                    -1, Image.SCALE_SMOOTH
            );
            imagePreviewLabel.setIcon(new ImageIcon(scaledPreview));

        } else {
            previewCards.show(previewContainer, "TEXT");
            currentLatexBuffer = canvas.getLatexRepresentation(); // Will now pull from the abstract class implementation
            textPreviewArea.setText(currentLatexBuffer);
            textPreviewArea.setCaretPosition(0); // Scroll to top
        }
    }

    private void performExport() {
        boolean isVisual = visRadio.isSelected();
        int formatIndex = formatComboBox.getSelectedIndex();

        String ext, desc;
        if (isVisual) {
            ext = formatIndex == 0 ? "png" : "jpg";
            desc = formatIndex == 0 ? "PNG Image (*.png)" : "JPEG Image (*.jpg)";
        } else {
            ext = formatIndex == 0 ? "tex" : "txt";
            desc = formatIndex == 0 ? "LaTeX Source (*.tex)" : "Plain Text (*.txt)";
        }

        // Open the File Chooser so they can pick Name and Location
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Export As...");
        chooser.setFileFilter(new FileNameExtensionFilter(desc, ext));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            // Append extension if the user forgot to type it
            if (!file.getName().toLowerCase().endsWith("." + ext)) {
                file = new File(file.getParentFile(), file.getName() + "." + ext);
            }

            try {
                if (isVisual) {
                    BufferedImage exportImg = currentVisualBuffer;
                    if (ext.equals("jpg")) {
                        // JPEG doesn't support transparency, fill background with white
                        exportImg = new BufferedImage(currentVisualBuffer.getWidth(), currentVisualBuffer.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2d = exportImg.createGraphics();
                        g2d.setColor(Color.WHITE);
                        g2d.fillRect(0, 0, exportImg.getWidth(), exportImg.getHeight());
                        g2d.drawImage(currentVisualBuffer, 0, 0, null);
                        g2d.dispose();
                    }
                    ImageIO.write(exportImg, ext, file);
                } else {
                    // Write LaTeX text to file
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(currentLatexBuffer);
                    }
                }

                JOptionPane.showMessageDialog(this, "Export successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Close dialog on success

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting file:\n" + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}