package com.financeanalyzer.ui;

import com.financeanalyzer.model.MarketDataFetcher;
import com.financeanalyzer.model.Candle;
import com.financeanalyzer.portfolio.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class PortfolioApp extends JFrame {

    private final Portfolio portfolio;
    private final List<Candle> candles;
    // Instruments négociables dans le simulateur : l'action AAPL au comptant et un future
    // sur AAPL utilisant le même flux de clôtures comme prix (approximation pédagogique :
    // en réalité le prix d'un future diffère du spot par le "coût de portage").
    private final List<Asset> tradableAssets;
    private int currentIndex;

    private JLabel dateLabel;
    private JLabel priceLabel;
    private JLabel priceChangeLabel;
    private JLabel cashLabel;
    private JLabel totalValueLabel;
    private JLabel pnlBadge;
    private DefaultTableModel tableModel;
    private JComboBox<Asset> instrumentSelector;

    // --- Palette ---
    private static final Color BG_COLOR = new Color(241, 243, 248);
    private static final Color HEADER_COLOR = new Color(23, 32, 56);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(45, 108, 223);
    private static final Color POSITIVE_COLOR = new Color(22, 163, 96);
    private static final Color NEGATIVE_COLOR = new Color(220, 53, 69);
    private static final Color MUTED_TEXT = new Color(130, 135, 150);

    // --- Polices ---
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_CARD_LABEL = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font FONT_CARD_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_TABLE = new Font("Segoe UI", Font.PLAIN, 16);
    private static final Font FONT_TABLE_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 17);

    public PortfolioApp(Portfolio portfolio, List<Candle> candles, List<Asset> tradableAssets, int startIndex) {
        this.portfolio = portfolio;
        this.candles = candles;
        this.tradableAssets = tradableAssets;
        this.currentIndex = startIndex;

        setTitle("Finance Analyzer");
        setSize(1300, 800);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        buildUI();
        refreshDisplay();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        add(buildHeaderBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 25));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(25, 35, 30, 35));
        add(body, BorderLayout.CENTER);

        body.add(buildCardsPanel(), BorderLayout.NORTH);
        body.add(buildTablePanel(), BorderLayout.CENTER);
        body.add(buildFooter(), BorderLayout.SOUTH);
    }

    // ---------- HEADER ----------
    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_COLOR);
        header.setBorder(new EmptyBorder(22, 35, 22, 35));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("📊  Suivi de portefeuille");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Simulation jour par jour — Apple Inc. (AAPL)");
        subtitle.setFont(FONT_SUBTITLE);
        subtitle.setForeground(new Color(180, 188, 210));
        subtitle.setBorder(new EmptyBorder(4, 0, 0, 0));

        titleBlock.add(title);
        titleBlock.add(subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        pnlBadge = new JLabel("", SwingConstants.CENTER);
        pnlBadge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnlBadge.setOpaque(true);
        pnlBadge.setBorder(new EmptyBorder(10, 22, 10, 22));
        header.add(pnlBadge, BorderLayout.EAST);

        return header;
    }

    // ---------- CARTES ----------
    private JPanel buildCardsPanel() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 20, 0));
        cards.setOpaque(false);

        dateLabel = makeCardValueLabel();
        priceLabel = makeCardValueLabel();
        cashLabel = makeCardValueLabel();
        totalValueLabel = makeCardValueLabel();
        priceChangeLabel = new JLabel();
        priceChangeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        cards.add(makeCard("📅  Date simulée", dateLabel, null));
        cards.add(makeCard("💵  Prix AAPL", priceLabel, priceChangeLabel));
        cards.add(makeCard("🏦  Cash disponible", cashLabel, null));
        cards.add(makeCard("💼  Valeur totale", totalValueLabel, null));

        return cards;
    }

    private JLabel makeCardValueLabel() {
        JLabel label = new JLabel();
        label.setFont(FONT_CARD_VALUE);
        label.setForeground(new Color(30, 33, 40));
        return label;
    }

    private JPanel makeCard(String title, JLabel valueLabel, JLabel extraLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 230, 236), 1, true),
                new EmptyBorder(20, 22, 20, 22)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_CARD_LABEL);
        titleLabel.setForeground(MUTED_TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);

        if (extraLabel != null) {
            extraLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(Box.createVerticalStrut(4));
            card.add(extraLabel);
        }

        return card;
    }

    // ---------- TABLEAU ----------
    private JScrollPane buildTablePanel() {
        String[] columns = {"Actif", "Type", "Quantité", "Prix moyen", "Prix actuel", "Valeur marché", "P&L latent"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setFont(FONT_TABLE);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(224, 234, 255));
        table.setBackground(CARD_COLOR);
        table.setGridColor(new Color(235, 235, 240));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Rendu spécial pour colorer le P&L (dernière colonne)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String text = value.toString();
                label.setForeground(text.startsWith("-") ? NEGATIVE_COLOR : POSITIVE_COLOR);
                return label;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HEADER);
        header.setBackground(new Color(233, 236, 244));
        header.setForeground(new Color(60, 65, 80));
        header.setPreferredSize(new Dimension(0, 44));
        header.setBorder(BorderFactory.createEmptyBorder());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(228, 230, 236)));
        scrollPane.getViewport().setBackground(CARD_COLOR);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        JLabel tableTitle = new JLabel("Positions détenues");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        wrapper.add(tableTitle, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return new JScrollPane(wrapper) {{
            setBorder(null);
            setViewportBorder(null);
        }};
    }

    // ---------- FOOTER / BOUTON ----------
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel progressLabel = new JLabel();
        progressLabel.setFont(FONT_SUBTITLE);
        progressLabel.setForeground(MUTED_TEXT);
        footer.add(progressLabel, BorderLayout.WEST);
        footer.setName("progressLabel");

        JLabel instrumentLabel = new JLabel("Instrument :");
        instrumentLabel.setFont(FONT_SUBTITLE);
        instrumentLabel.setForeground(MUTED_TEXT);

        instrumentSelector = new JComboBox<>(tradableAssets.toArray(new Asset[0]));
        instrumentSelector.setFont(FONT_SUBTITLE);
        // Asset.toString() affiche "TICKER (Nom) [TYPE]" : on voit directement si on
        // négocie l'action au comptant ou le future à effet de levier.

        JLabel quantityLabel = new JLabel("Quantité :");
        quantityLabel.setFont(FONT_SUBTITLE);
        quantityLabel.setForeground(MUTED_TEXT);

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        quantitySpinner.setFont(FONT_SUBTITLE);
        quantitySpinner.setPreferredSize(new Dimension(90, 48));

        JButton buyButton = makeButton("Acheter", POSITIVE_COLOR);
        buyButton.addActionListener(e ->
                executeTrade(TransactionType.BUY, (Integer) quantitySpinner.getValue()));

        JButton sellButton = makeButton("Vendre", NEGATIVE_COLOR);
        sellButton.addActionListener(e ->
                executeTrade(TransactionType.SELL, (Integer) quantitySpinner.getValue()));

        JButton swapButton = makeButton("Swap de taux…", new Color(120, 90, 200));
        swapButton.addActionListener(e -> openSwapDialog());

        JButton nextDayButton = makeButton("Jour suivant  →", ACCENT_COLOR);
        nextDayButton.addActionListener(e -> {
            if (currentIndex < candles.size() - 1) {
                currentIndex++;
                refreshDisplay();
            } else {
                JOptionPane.showMessageDialog(this, "Fin des données disponibles.");
            }
        });

        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonWrap.setOpaque(false);
        buttonWrap.add(instrumentLabel);
        buttonWrap.add(instrumentSelector);
        buttonWrap.add(quantityLabel);
        buttonWrap.add(quantitySpinner);
        buttonWrap.add(buyButton);
        buttonWrap.add(sellButton);
        buttonWrap.add(swapButton);
        buttonWrap.add(nextDayButton);
        footer.add(buttonWrap, BorderLayout.EAST);

        this.progressLabelRef = progressLabel;
        return footer;
    }

    private JButton makeButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(14, 34, 14, 34));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Exécute la transaction au cours de clôture du jour simulé, sur l'instrument sélectionné
    // (action ou future) ; affiche l'erreur si elle est refusée (fonds ou marge insuffisants).
    private void executeTrade(TransactionType type, int quantity) {
        Candle currentCandle = candles.get(currentIndex);
        Asset selectedAsset = (Asset) instrumentSelector.getSelectedItem();
        Transaction transaction = new Transaction(
                selectedAsset, type, quantity, currentCandle.getClose(), currentCandle.getDate());
        try {
            portfolio.applyTransaction(transaction);
        } catch (InsufficientFundsException | IllegalStateException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Transaction refusée", JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshDisplay();
    }

    // Ouvre une petite calculette pédagogique de swap de taux : elle affiche les deux jambes
    // (fixe / variable) et le règlement net, et peut l'appliquer réellement au cash du
    // portefeuille via Portfolio.settleSwap — sans jamais créer de position (un swap ne
    // s'achète pas, il génère seulement des règlements périodiques).
    private void openSwapDialog() {
        JDialog dialog = new JDialog(this, "Simuler un swap de taux", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField notionalField = new JTextField("1000000", 10);
        JTextField fixedRateField = new JTextField("3.0", 10);
        JTextField floatingRateField = new JTextField("2.5", 10);
        JComboBox<Integer> frequencyField = new JComboBox<>(new Integer[]{1, 2, 4, 12});
        frequencyField.setSelectedItem(4);
        JComboBox<String> directionField = new JComboBox<>(new String[]{
                "Je paie le fixe, je reçois le variable",
                "Je reçois le fixe, je paie le variable"});
        JLabel resultLabel = new JLabel(" ");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        int row = 0;
        row = addDialogRow(dialog, c, row, "Notionnel ($) :", notionalField);
        row = addDialogRow(dialog, c, row, "Taux fixe (%) :", fixedRateField);
        row = addDialogRow(dialog, c, row, "Taux variable courant (%) :", floatingRateField);
        row = addDialogRow(dialog, c, row, "Paiements / an :", frequencyField);
        row = addDialogRow(dialog, c, row, "Position :", directionField);

        c.gridx = 0;
        c.gridy = row++;
        c.gridwidth = 2;
        dialog.add(resultLabel, c);

        JButton computeButton = new JButton("Calculer le règlement net");
        JButton applyButton = makeButton("Appliquer au portefeuille", ACCENT_COLOR);
        applyButton.setEnabled(false);

        computeButton.addActionListener(e -> {
            try {
                InterestRateSwap swap = buildSwapFromDialog(
                        notionalField, fixedRateField, frequencyField, directionField);
                double floatingRate = Double.parseDouble(floatingRateField.getText().trim()) / 100.0;
                double net = swap.netSettlement(floatingRate);
                resultLabel.setText(String.format(
                        "<html>Jambe fixe : %.2f $ &nbsp;|&nbsp; Jambe variable : %.2f $"
                                + "<br>Règlement net reçu : <b>%.2f $</b> par période%s</html>",
                        swap.fixedLegPayment(), swap.floatingLegPayment(floatingRate), net,
                        net < 0 ? " (à payer)" : ""));
                applyButton.setEnabled(true);
            } catch (NumberFormatException ex) {
                resultLabel.setText("Merci de saisir des nombres valides.");
                applyButton.setEnabled(false);
            } catch (IllegalArgumentException ex) {
                resultLabel.setText(ex.getMessage());
                applyButton.setEnabled(false);
            }
        });

        applyButton.addActionListener(e -> {
            try {
                InterestRateSwap swap = buildSwapFromDialog(
                        notionalField, fixedRateField, frequencyField, directionField);
                double floatingRate = Double.parseDouble(floatingRateField.getText().trim()) / 100.0;
                portfolio.settleSwap(swap, 1, floatingRate);
                refreshDisplay();
                dialog.dispose();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Règlement refusé", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(computeButton);
        buttons.add(applyButton);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        dialog.add(buttons, c);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private int addDialogRow(JDialog dialog, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = row;
        dialog.add(new JLabel(label), c);
        c.gridx = 1;
        dialog.add(field, c);
        return row + 1;
    }

    private InterestRateSwap buildSwapFromDialog(JTextField notionalField, JTextField fixedRateField,
                                                  JComboBox<Integer> frequencyField,
                                                  JComboBox<String> directionField) {
        double notional = Double.parseDouble(notionalField.getText().trim());
        double fixedRate = Double.parseDouble(fixedRateField.getText().trim()) / 100.0;
        int frequency = (Integer) frequencyField.getSelectedItem();
        boolean payerFixed = directionField.getSelectedIndex() == 0;
        // Échéance arbitraire : ce dialogue ne sert qu'à calculer/régler une période, pas à
        // suivre un swap dans la durée.
        LocalDate maturity = candles.get(candles.size() - 1).getDate().plusYears(3);
        return new InterestRateSwap("SWAP-SIM", "Swap de taux simulé", "USD",
                notional, fixedRate, "EURIBOR 3M", maturity, frequency, payerFixed);
    }

    private JLabel progressLabelRef;

    // ---------- LOGIQUE D'AFFICHAGE ----------
    private void refreshDisplay() {
        Candle currentCandle = candles.get(currentIndex);
        double currentPrice = currentCandle.getClose();

        dateLabel.setText(currentCandle.getDate().toString());
        priceLabel.setText(String.format("%.2f $", currentPrice));

        if (currentIndex > 0) {
            double previousClose = candles.get(currentIndex - 1).getClose();
            double changePct = ((currentPrice - previousClose) / previousClose) * 100;
            priceChangeLabel.setText((changePct >= 0 ? "▲ +" : "▼ ") + String.format("%.2f %%", changePct));
            priceChangeLabel.setForeground(changePct >= 0 ? POSITIVE_COLOR : NEGATIVE_COLOR);
        } else {
            priceChangeLabel.setText("—");
            priceChangeLabel.setForeground(MUTED_TEXT);
        }

        cashLabel.setText(String.format("%.2f $", portfolio.getCash()));

        // Tous les instruments négociables (action et future) partagent ici le même prix
        // de clôture, utilisé comme proxy pédagogique du prix du future (voir commentaire
        // sur le champ tradableAssets).
        Map<Asset, Double> currentPrices = new HashMap<>();
        for (Asset asset : tradableAssets) {
            currentPrices.put(asset, currentPrice);
        }

        double totalValue = portfolio.getTotalValue(currentPrices);
        totalValueLabel.setText(String.format("%.2f $", totalValue));
        totalValueLabel.setForeground(ACCENT_COLOR);

        double totalPnl = 0;
        for (Position position : portfolio.getPositions().values()) {
            totalPnl += position.getUnrealizedPnL(currentPrice);
        }
        boolean positive = totalPnl >= 0;
        pnlBadge.setText((positive ? "▲ +" : "▼ ") + String.format("%.2f $", totalPnl));
        pnlBadge.setBackground(positive ? new Color(22, 163, 96, 60) : new Color(220, 53, 69, 60));
        pnlBadge.setForeground(positive ? new Color(150, 255, 200) : new Color(255, 190, 195));

        tableModel.setRowCount(0);
        for (Position position : portfolio.getPositions().values()) {
            double marketValue = position.getMarketValue(currentPrice);
            double pnl = position.getUnrealizedPnL(currentPrice);

            tableModel.addRow(new Object[]{
                    position.getAsset().getTicker(),
                    position.getAsset().getType(),
                    position.getQuantity(),
                    String.format("%.2f $", position.getAveragePrice()),
                    String.format("%.2f $", currentPrice),
                    String.format("%.2f $", marketValue),
                    (pnl >= 0 ? "+" : "") + String.format("%.2f $", pnl)
            });
        }

        if (progressLabelRef != null) {
            progressLabelRef.setText("Jour " + (currentIndex + 1) + " / " + candles.size());
        }
    }

    public static void main(String[] args) throws Exception {
        List<Candle> candles = MarketDataFetcher.sortByDateAscending(
                MarketDataFetcher.loadFromResource("aapl_daily.json"));

        Asset aaplStock = new Stock("AAPL", "Apple Inc.", "USD", "Technology");
        // Future sur AAPL, à titre pédagogique : mêmes clôtures que l'action comme prix,
        // mais négocié avec 20 % de marge initiale (10 actions par contrat), pour comparer
        // directement l'exposition au comptant et l'exposition à effet de levier.
        Asset aaplFuture = new Future("AAPLF", "Future AAPL (1 mois)", "USD", "AAPL",
                10, candles.get(candles.size() - 1).getDate().plusMonths(1), 0.20, 0.15);
        List<Asset> tradableAssets = List.of(aaplStock, aaplFuture);

        Portfolio portfolio = new Portfolio(10000.0);

        Candle firstCandle = candles.get(0);
        portfolio.applyTransaction(new Transaction(
                aaplStock, TransactionType.BUY, 10, firstCandle.getClose(), firstCandle.getDate()));

        SwingUtilities.invokeLater(() -> {
            PortfolioApp app = new PortfolioApp(portfolio, candles, tradableAssets, 0);
            app.setVisible(true);
        });
    }
}