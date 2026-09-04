import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SmartInventoryManager {

    // =========================================================
    // PRODUCT
    // =========================================================

    static class Product {
        int id;
        String name;
        String category;
        double price;
        int quantity;
        int reorderLevel;

        Product(int id, String name, String category,
                double price, int quantity, int reorderLevel) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
        }

        boolean isLowStock() {
            return quantity <= reorderLevel;
        }
    }

    // =========================================================
    // CUSTOM HASH TABLE
    // =========================================================

    static class HashNode {
        Product product;
        HashNode next;

        HashNode(Product product) {
            this.product = product;
        }
    }

    static class MyHashTable {

        HashNode[] table;
        int size;

        MyHashTable(int capacity) {
            table = new HashNode[capacity];
        }

        int hash(int key) {
            return Math.abs(key) % table.length;
        }

        void put(Product product) {

            int index = hash(product.id);

            HashNode current = table[index];

            while (current != null) {

                if (current.product.id == product.id) {
                    current.product = product;
                    return;
                }

                current = current.next;
            }

            HashNode node = new HashNode(product);
            node.next = table[index];
            table[index] = node;

            size++;
        }

        Product get(int id) {

            int index = hash(id);

            HashNode current = table[index];

            while (current != null) {

                if (current.product.id == id) {
                    return current.product;
                }

                current = current.next;
            }

            return null;
        }

        Product remove(int id) {

            int index = hash(id);

            HashNode current = table[index];
            HashNode previous = null;

            while (current != null) {

                if (current.product.id == id) {

                    if (previous == null) {
                        table[index] = current.next;
                    } else {
                        previous.next = current.next;
                    }

                    size--;

                    return current.product;
                }

                previous = current;
                current = current.next;
            }

            return null;
        }
    }

    // =========================================================
    // BST FOR PRICE ORDER
    // =========================================================

    static class BSTNode {

        Product product;
        BSTNode left;
        BSTNode right;

        BSTNode(Product product) {
            this.product = product;
        }
    }

    static class PriceBST {

        BSTNode root;

        void insert(Product product) {

            root = insert(root, product);
        }

        BSTNode insert(BSTNode root, Product product) {

            if (root == null) {
                return new BSTNode(product);
            }

            if (product.price < root.product.price) {
                root.left = insert(root.left, product);
            } else {
                root.right = insert(root.right, product);
            }

            return root;
        }

        void inorder(BSTNode root, List<Product> result) {

            if (root == null) {
                return;
            }

            inorder(root.left, result);
            result.add(root.product);
            inorder(root.right, result);
        }
    }

    // =========================================================
    // CUSTOM QUEUE
    // =========================================================

    static class QueueNode {

        Product product;
        QueueNode next;

        QueueNode(Product product) {
            this.product = product;
        }
    }

    static class ProductQueue {

        QueueNode front;
        QueueNode rear;

        void enqueue(Product product) {

            QueueNode node = new QueueNode(product);

            if (rear == null) {
                front = rear = node;
                return;
            }

            rear.next = node;
            rear = node;
        }

        Product dequeue() {

            if (front == null) {
                return null;
            }

            Product product = front.product;

            front = front.next;

            if (front == null) {
                rear = null;
            }

            return product;
        }

        boolean isEmpty() {
            return front == null;
        }
    }

    // =========================================================
    // CUSTOM STACK FOR UNDO
    // =========================================================

    static class StockAction {

        int productId;
        int oldQuantity;
        int newQuantity;

        StockAction(int productId, int oldQuantity, int newQuantity) {
            this.productId = productId;
            this.oldQuantity = oldQuantity;
            this.newQuantity = newQuantity;
        }
    }

    static class StackNode {

        StockAction action;
        StackNode next;

        StackNode(StockAction action) {
            this.action = action;
        }
    }

    static class ProductStack {

        StackNode top;

        void push(StockAction action) {

            StackNode node = new StackNode(action);

            node.next = top;
            top = node;
        }

        StockAction pop() {

            if (top == null) {
                return null;
            }

            StockAction action = top.action;

            top = top.next;

            return action;
        }

        boolean isEmpty() {
            return top == null;
        }
    }

    // =========================================================
    // MAX HEAP FOR RESTOCK PRIORITY
    // =========================================================

    static class MaxHeap {

        ArrayList<Product> heap = new ArrayList<>();

        int priority(Product p) {

            int shortage = p.reorderLevel - p.quantity;

            if (shortage < 0) {
                shortage = 0;
            }

            return shortage;
        }

        void add(Product product) {

            heap.add(product);

            int index = heap.size() - 1;

            while (index > 0) {

                int parent = (index - 1) / 2;

                if (priority(heap.get(parent)) >= priority(heap.get(index))) {
                    break;
                }

                Product temp = heap.get(parent);
                heap.set(parent, heap.get(index));
                heap.set(index, temp);

                index = parent;
            }
        }

        Product removeMax() {

            if (heap.isEmpty()) {
                return null;
            }

            Product result = heap.get(0);

            Product last = heap.remove(heap.size() - 1);

            if (!heap.isEmpty()) {

                heap.set(0, last);

                int index = 0;

                while (true) {

                    int left = index * 2 + 1;
                    int right = index * 2 + 2;

                    int largest = index;

                    if (left < heap.size()
                            && priority(heap.get(left)) > priority(heap.get(largest))) {
                        largest = left;
                    }

                    if (right < heap.size()
                            && priority(heap.get(right)) > priority(heap.get(largest))) {
                        largest = right;
                    }

                    if (largest == index) {
                        break;
                    }

                    Product temp = heap.get(index);
                    heap.set(index, heap.get(largest));
                    heap.set(largest, temp);

                    index = largest;
                }
            }

            return result;
        }

        boolean isEmpty() {
            return heap.isEmpty();
        }
    }

    // =========================================================
    // TRANSACTION
    // =========================================================

    static class Transaction {

        String type;
        int productId;
        String description;

        Transaction(String type, int productId, String description) {
            this.type = type;
            this.productId = productId;
            this.description = description;
        }
    }

    // =========================================================
    // GLOBAL DATA
    // =========================================================

    static MyHashTable inventory = new MyHashTable(101);

    static ProductStack undoStack = new ProductStack();

    static ProductQueue transactionQueue = new ProductQueue();

    static ArrayList<Transaction> transactions = new ArrayList<>();

    static JFrame frame;

    static JPanel contentPanel;

    static JLabel totalProductsLabel;
    static JLabel totalUnitsLabel;
    static JLabel lowStockLabel;
    static JLabel inventoryValueLabel;

    static JTable table;

    static DefaultTableModel tableModel;

    static JTextField idField;
    static JTextField nameField;
    static JTextField categoryField;
    static JTextField priceField;
    static JTextField quantityField;
    static JTextField reorderField;
    static JTextField searchField;

    static JLabel statusLabel;

    // =========================================================
    // COLORS
    // =========================================================

    static Color BACKGROUND = new Color(18, 24, 32);
    static Color SIDEBAR = new Color(13, 18, 25);
    static Color CARD = new Color(28, 36, 47);
    static Color CARD2 = new Color(34, 44, 57);
    static Color TEXT = new Color(235, 240, 245);
    static Color MUTED = new Color(150, 162, 175);
    static Color ACCENT = new Color(60, 140, 230);
    static Color SUCCESS = new Color(65, 180, 120);
    static Color WARNING = new Color(235, 170, 65);
    static Color DANGER = new Color(220, 80, 85);

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        loadSampleData();

        SwingUtilities.invokeLater(() -> {

            createGUI();

            showDashboard();
        });
    }

    // =========================================================
    // SAMPLE DATA
    // =========================================================

    static void loadSampleData() {

        addSample(new Product(
                101,
                "Laptop",
                "Electronics",
                65000,
                12,
                5
        ));

        addSample(new Product(
                102,
                "Keyboard",
                "Electronics",
                1800,
                25,
                8
        ));

        addSample(new Product(
                103,
                "Mouse",
                "Electronics",
                900,
                6,
                10
        ));

        addSample(new Product(
                104,
                "Office Chair",
                "Furniture",
                8500,
                4,
                5
        ));

        addSample(new Product(
                105,
                "Notebook",
                "Stationery",
                120,
                50,
                15
        ));

        addSample(new Product(
                106,
                "Monitor",
                "Electronics",
                18000,
                9,
                5
        ));

        addSample(new Product(
                107,
                "Desk Lamp",
                "Furniture",
                1500,
                7,
                10
        ));
    }

    static void addSample(Product product) {

        inventory.put(product);
    }

    // =========================================================
    // GUI
    // =========================================================

    static void createGUI() {

        frame = new JFrame("Smart Inventory & Stock Manager");

        frame.setSize(1250, 760);

        frame.setMinimumSize(new Dimension(1050, 650));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());

        frame.getContentPane().setBackground(BACKGROUND);

        // SIDEBAR
        JPanel sidebar = createSidebar();

        frame.add(sidebar, BorderLayout.WEST);

        // MAIN CONTENT
        contentPanel = new JPanel(new BorderLayout());

        contentPanel.setBackground(BACKGROUND);

        contentPanel.setBorder(new EmptyBorder(
                25,
                25,
                20,
                25
        ));

        frame.add(contentPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    // =========================================================
    // SIDEBAR
    // =========================================================

    static JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setPreferredSize(new Dimension(220, 760));

        sidebar.setBackground(SIDEBAR);

        sidebar.setLayout(new BorderLayout());

        sidebar.setBorder(new EmptyBorder(
                25,
                15,
                20,
                15
        ));

        JPanel top = new JPanel();

        top.setOpaque(false);

        top.setLayout(new BoxLayout(
                top,
                BoxLayout.Y_AXIS
        ));

        JLabel title = new JLabel("INVENTORY");

        title.setForeground(TEXT);

        title.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                24
        ));

        JLabel subtitle = new JLabel("STOCK MANAGER");

        subtitle.setForeground(ACCENT);

        subtitle.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                13
        ));

        top.add(title);

        top.add(Box.createVerticalStrut(3));

        top.add(subtitle);

        top.add(Box.createVerticalStrut(35));

        sidebarButton(top, "Dashboard", () -> showDashboard());

        sidebarButton(top, "View Inventory", () -> showInventory());

        sidebarButton(top, "Search Product", () -> showSearch());

        sidebarButton(top, "Add Product", () -> showAddProduct());

        sidebarButton(top, "Remove Product", () -> showRemoveProduct());

        sidebarButton(top, "Update Stock", () -> showUpdateStock());

        sidebarButton(top, "Low Stock", () -> showLowStock());

        sidebarButton(top, "Restock Priority", () -> showRestockPriority());

        sidebarButton(top, "BST Price Order", () -> showBST());

        sidebarButton(top, "Statistics", () -> showStatistics());

        sidebarButton(top, "Transactions", () -> showTransactions());

        sidebarButton(top, "Undo Stock Update", () -> undoStockUpdate());

        sidebar.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel();

        bottom.setOpaque(false);

        bottom.setLayout(new BoxLayout(
                bottom,
                BoxLayout.Y_AXIS
        ));

        JLabel line = new JLabel(
                "Data Structures & Algorithms"
        );

        line.setForeground(MUTED);

        line.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                11
        ));

        bottom.add(line);

        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    static void sidebarButton(
            JPanel parent,
            String text,
            Runnable action
    ) {

        JButton button = new JButton(text);

        button.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        43
                )
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.setHorizontalAlignment(
                SwingConstants.LEFT
        );

        button.setForeground(TEXT);

        button.setBackground(SIDEBAR);

        button.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                14
        ));

        button.setBorder(
                new EmptyBorder(
                        0,
                        12,
                        0,
                        0
                )
        );

        button.setFocusPainted(false);

        button.addActionListener(e -> action.run());

        parent.add(button);

        parent.add(Box.createVerticalStrut(5));
    }

    // =========================================================
    // COMMON HEADER
    // =========================================================

    static JPanel createHeader(
            String title,
            String subtitle
    ) {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
        ));

        JLabel titleLabel = new JLabel(title);

        titleLabel.setForeground(TEXT);

        titleLabel.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                28
        ));

        JLabel subtitleLabel = new JLabel(subtitle);

        subtitleLabel.setForeground(MUTED);

        subtitleLabel.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(5));

        panel.add(subtitleLabel);

        panel.add(Box.createVerticalStrut(25));

        return panel;
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    static void showDashboard() {

        contentPanel.removeAll();

        JPanel main = new JPanel(new BorderLayout());

        main.setBackground(BACKGROUND);

        JPanel header = createHeader(
                "Smart Inventory Dashboard",
                "Manage products, monitor stock and analyze inventory"
        );

        main.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();

        center.setBackground(BACKGROUND);

        center.setLayout(new BoxLayout(
                center,
                BoxLayout.Y_AXIS
        ));

        // CARDS
        JPanel cards = new JPanel(
                new GridLayout(
                        1,
                        4,
                        15,
                        0
                )
        );

        cards.setBackground(BACKGROUND);

        totalProductsLabel = new JLabel();

        totalUnitsLabel = new JLabel();

        lowStockLabel = new JLabel();

        inventoryValueLabel = new JLabel();

        cards.add(
                createCard(
                        "TOTAL PRODUCTS",
                        totalProductsLabel,
                        ACCENT
                )
        );

        cards.add(
                createCard(
                        "TOTAL UNITS",
                        totalUnitsLabel,
                        SUCCESS
                )
        );

        cards.add(
                createCard(
                        "LOW STOCK",
                        lowStockLabel,
                        WARNING
                )
        );

        cards.add(
                createCard(
                        "INVENTORY VALUE",
                        inventoryValueLabel,
                        DANGER
                )
        );

        center.add(cards);

        center.add(Box.createVerticalStrut(25));

        // TABLE TITLE
        JLabel overview = new JLabel(
                "Inventory Overview"
        );

        overview.setForeground(TEXT);

        overview.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                18
        ));

        center.add(overview);

        center.add(Box.createVerticalStrut(10));

        // TABLE
        createTable();

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(null);

        scrollPane.getViewport().setBackground(CARD);

        center.add(scrollPane);

        main.add(center, BorderLayout.CENTER);

        contentPanel.add(main);

        updateDashboard();

        refreshTable();

        contentPanel.revalidate();

        contentPanel.repaint();
    }

    static JPanel createCard(
            String title,
            JLabel valueLabel,
            Color accent
    ) {

        JPanel card = new JPanel();

        card.setBackground(CARD);

        card.setBorder(
                new EmptyBorder(
                        18,
                        20,
                        18,
                        20
                )
        );

        card.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title);

        titleLabel.setForeground(MUTED);

        titleLabel.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                11
        ));

        valueLabel.setForeground(accent);

        valueLabel.setFont(new Font(
                "Segoe UI",
                Font.BOLD,
                25
        ));

        card.add(titleLabel, BorderLayout.NORTH);

        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // =========================================================
    // TABLE
    // =========================================================

    static void createTable() {

        String[] columns = {
                "ID",
                "Product",
                "Category",
                "Price",
                "Quantity",
                "Reorder",
                "Status"
        };

        tableModel = new DefaultTableModel(
                columns,
                0
        ) {

            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setRowHeight(35);

        table.setBackground(CARD);

        table.setForeground(TEXT);

        table.setGridColor(
                new Color(50, 60, 72)
        );

        table.setFont(new Font(
                "Segoe UI",
                Font.PLAIN,
                13
        ));

        table.getTableHeader().setBackground(
                CARD2
        );

        table.getTableHeader().setForeground(
                TEXT
        );

        table.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );
    }

    static void refreshTable() {

        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        ArrayList<Product> products =
                getAllProducts();

        for (Product p : products) {

            String status =
                    p.isLowStock()
                            ? "LOW STOCK"
                            : "IN STOCK";

            tableModel.addRow(
                    new Object[]{
                            p.id,
                            p.name,
                            p.category,
                            String.format(
                                    "₹%.2f",
                                    p.price
                            ),
                            p.quantity,
                            p.reorderLevel,
                            status
                    }
            );
        }
    }

    // =========================================================
    // GET ALL PRODUCTS
    // =========================================================

    static ArrayList<Product> getAllProducts() {

        ArrayList<Product> list =
                new ArrayList<>();

        for (HashNode bucket : inventory.table) {

            HashNode current = bucket;

            while (current != null) {

                list.add(current.product);

                current = current.next;
            }
        }

        return list;
    }

    // =========================================================
    // DASHBOARD UPDATE
    // =========================================================

    static void updateDashboard() {

        ArrayList<Product> products =
                getAllProducts();

        int totalUnits = 0;

        int lowStock = 0;

        double totalValue = 0;

        for (Product p : products) {

            totalUnits += p.quantity;

            if (p.isLowStock()) {
                lowStock++;
            }

            totalValue +=
                    p.price * p.quantity;
        }

        if (totalProductsLabel != null) {

            totalProductsLabel.setText(
                    String.valueOf(products.size())
            );
        }

        if (totalUnitsLabel != null) {

            totalUnitsLabel.setText(
                    String.valueOf(totalUnits)
            );
        }

        if (lowStockLabel != null) {

            lowStockLabel.setText(
                    String.valueOf(lowStock)
            );
        }

        if (inventoryValueLabel != null) {

            inventoryValueLabel.setText(
                    String.format(
                            "₹%.0f",
                            totalValue
                    )
            );
        }
    }

    // =========================================================
    // INVENTORY
    // =========================================================

    static void showInventory() {

        contentPanel.removeAll();

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setBackground(BACKGROUND);

        panel.add(
                createHeader(
                        "View Inventory",
                        "Complete list of products currently stored"
                ),
                BorderLayout.NORTH
        );

        createTable();

        refreshTable();

        JScrollPane scroll =
                new JScrollPane(table);

        scroll.setBorder(null);

        panel.add(
                scroll,
                BorderLayout.CENTER
        );

        contentPanel.add(panel);

        refreshContent();
    }

    // =========================================================
    // SEARCH
    // =========================================================

    static void showSearch() {

        contentPanel.removeAll();

        JPanel main = new JPanel(
                new BorderLayout()
        );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Search Product",
                        "Fast product lookup using the custom Hash Table"
                ),
                BorderLayout.NORTH
        );

        JPanel center = new JPanel();

        center.setBackground(BACKGROUND);

        center.setLayout(new BoxLayout(
                center,
                BoxLayout.Y_AXIS
        ));

        JPanel searchPanel =
                createFormPanel();

        JTextField field =
                new JTextField();

        addFormRow(
                searchPanel,
                "Product ID",
                field
        );

        JButton searchButton =
                createActionButton(
                        "Search",
                        ACCENT
                );

        JTextArea result =
                createTextArea();

        searchButton.addActionListener(e -> {

            try {

                int id = Integer.parseInt(
                        field.getText().trim()
                );

                Product p =
                        inventory.get(id);

                if (p == null) {

                    result.setText(
                            "Product not found."
                    );

                    setStatus(
                            "Product not found"
                    );

                    return;
                }

                result.setText(
                        productDetails(p)
                );

                setStatus(
                        "Product found using Hash Table"
                );

            } catch (Exception ex) {

                result.setText(
                        "Please enter a valid Product ID."
                );
            }
        });

        searchPanel.add(searchButton);

        center.add(searchPanel);

        center.add(
                Box.createVerticalStrut(20)
        );

        center.add(
                createOutputScroll(result)
        );

        main.add(
                center,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // ADD PRODUCT
    // =========================================================

    static void showAddProduct() {

        contentPanel.removeAll();

        JPanel main = new JPanel(
                new BorderLayout()
        );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Add Product",
                        "Add a new product to the inventory"
                ),
                BorderLayout.NORTH
        );

        JPanel form =
                createProductForm();

        JButton addButton =
                createActionButton(
                        "Add Product",
                        SUCCESS
                );

        JButton clearButton =
                createActionButton(
                        "Clear Fields",
                        CARD2
                );

        addButton.addActionListener(
                e -> addProduct()
        );

        clearButton.addActionListener(
                e -> clearFields()
        );

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT
                        )
                );

        buttons.setBackground(BACKGROUND);

        buttons.add(addButton);

        buttons.add(clearButton);

        JPanel container =
                new JPanel(
                        new BorderLayout()
                );

        container.setBackground(BACKGROUND);

        container.add(
                form,
                BorderLayout.NORTH
        );

        container.add(
                buttons,
                BorderLayout.CENTER
        );

        main.add(
                container,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    static JPanel createProductForm() {

        JPanel form =
                new JPanel(
                        new GridLayout(
                                6,
                                2,
                                15,
                                12
                        )
                );

        form.setBackground(BACKGROUND);

        idField = new JTextField();

        nameField = new JTextField();

        categoryField = new JTextField();

        priceField = new JTextField();

        quantityField = new JTextField();

        reorderField = new JTextField();

        addFormRow(
                form,
                "Product ID",
                idField
        );

        addFormRow(
                form,
                "Product Name",
                nameField
        );

        addFormRow(
                form,
                "Category",
                categoryField
        );

        addFormRow(
                form,
                "Price",
                priceField
        );

        addFormRow(
                form,
                "Quantity",
                quantityField
        );

        addFormRow(
                form,
                "Reorder Level",
                reorderField
        );

        return form;
    }

    // =========================================================
    // REMOVE PRODUCT
    // =========================================================

    static void showRemoveProduct() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Remove Product",
                        "Delete a product using its Product ID"
                ),
                BorderLayout.NORTH
        );

        JPanel form =
                createFormPanel();

        JTextField field =
                new JTextField();

        addFormRow(
                form,
                "Product ID",
                field
        );

        JButton button =
                createActionButton(
                        "Remove Product",
                        DANGER
                );

        JTextArea output =
                createTextArea();

        button.addActionListener(e -> {

            try {

                int id = Integer.parseInt(
                        field.getText().trim()
                );

                Product removed =
                        inventory.remove(id);

                if (removed == null) {

                    output.setText(
                            "Product not found."
                    );

                    setStatus(
                            "Remove failed"
                    );

                    return;
                }

                transactions.add(
                        new Transaction(
                                "REMOVE",
                                id,
                                "Removed " +
                                        removed.name
                        )
                );

                output.setText(
                        "Product removed successfully.\n\n"
                                + productDetails(
                                removed
                        )
                );

                setStatus(
                        "Product removed"
                );

            } catch (Exception ex) {

                output.setText(
                        "Please enter a valid Product ID."
                );
            }
        });

        form.add(button);

        JPanel center =
                new JPanel(
                        new BorderLayout()
                );

        center.setBackground(BACKGROUND);

        center.add(
                form,
                BorderLayout.NORTH
        );

        center.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        main.add(
                center,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // UPDATE STOCK
    // =========================================================

    static void showUpdateStock() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Update Stock",
                        "Change product quantity using the Stack-based undo system"
                ),
                BorderLayout.NORTH
        );

        JPanel form =
                createFormPanel();

        JTextField id =
                new JTextField();

        JTextField quantity =
                new JTextField();

        addFormRow(
                form,
                "Product ID",
                id
        );

        addFormRow(
                form,
                "New Quantity",
                quantity
        );

        JButton update =
                createActionButton(
                        "Update Stock",
                        ACCENT
                );

        JTextArea output =
                createTextArea();

        update.addActionListener(e -> {

            try {

                int productId =
                        Integer.parseInt(
                                id.getText().trim()
                        );

                int newQuantity =
                        Integer.parseInt(
                                quantity.getText().trim()
                        );

                if (newQuantity < 0) {

                    output.setText(
                            "Quantity cannot be negative."
                    );

                    return;
                }

                Product p =
                        inventory.get(productId);

                if (p == null) {

                    output.setText(
                            "Product not found."
                    );

                    return;
                }

                int oldQuantity =
                        p.quantity;

                p.quantity =
                        newQuantity;

                undoStack.push(
                        new StockAction(
                                productId,
                                oldQuantity,
                                newQuantity
                        )
                );

                transactions.add(
                        new Transaction(
                                "UPDATE",
                                productId,
                                "Stock changed from "
                                        + oldQuantity
                                        + " to "
                                        + newQuantity
                        )
                );

                output.setText(
                        "Stock updated successfully.\n\n"
                                + productDetails(p)
                                + "\n\n"
                                + "Undo is available using the Stack."
                );

                setStatus(
                        "Stock updated"
                );

            } catch (Exception ex) {

                output.setText(
                        "Please enter valid values."
                );
            }
        });

        form.add(update);

        JPanel center =
                new JPanel(
                        new BorderLayout()
                );

        center.setBackground(BACKGROUND);

        center.add(
                form,
                BorderLayout.NORTH
        );

        center.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        main.add(
                center,
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // UNDO
    // =========================================================

    static void undoStockUpdate() {

        StockAction action =
                undoStack.pop();

        if (action == null) {

            setStatus(
                    "Nothing to undo"
            );

            JOptionPane.showMessageDialog(
                    frame,
                    "No stock update is available to undo."
            );

            return;
        }

        Product p =
                inventory.get(
                        action.productId
                );

        if (p != null) {

            p.quantity =
                    action.oldQuantity;

            transactions.add(
                    new Transaction(
                            "UNDO",
                            p.id,
                            "Stock restored to "
                                    + p.quantity
                    )
            );

            setStatus(
                    "Last stock update undone"
            );

            showDashboard();
        }
    }

    // =========================================================
    // LOW STOCK
    // =========================================================

    static void showLowStock() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Low Stock",
                        "Products that have reached their reorder level"
                ),
                BorderLayout.NORTH
        );

        JTextArea output =
                createTextArea();

        StringBuilder sb =
                new StringBuilder();

        ArrayList<Product> products =
                getAllProducts();

        int count = 0;

        for (Product p : products) {

            if (p.isLowStock()) {

                count++;

                sb.append(
                        p.id
                                + "  |  "
                                + p.name
                                + "  |  Qty: "
                                + p.quantity
                                + "  |  Reorder: "
                                + p.reorderLevel
                                + "\n"
                );
            }
        }

        if (count == 0) {

            sb.append(
                    "No low-stock products."
            );

        } else {

            sb.insert(
                    0,
                    "LOW STOCK PRODUCTS\n"
                            + "--------------------------\n\n"
            );

            sb.append(
                    "\nTotal low-stock products: "
                            + count
            );
        }

        output.setText(
                sb.toString()
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // RESTOCK PRIORITY - MAX HEAP
    // =========================================================

    static void showRestockPriority() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Restock Priority",
                        "Max Heap ranks products according to stock shortage"
                ),
                BorderLayout.NORTH
        );

        MaxHeap heap =
                new MaxHeap();

        for (Product p : getAllProducts()) {

            if (p.isLowStock()) {
                heap.add(p);
            }
        }

        JTextArea output =
                createTextArea();

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "RESTOCK PRIORITY\n"
                        + "============================\n\n"
        );

        int rank = 1;

        while (!heap.isEmpty()) {

            Product p =
                    heap.removeMax();

            int shortage =
                    p.reorderLevel - p.quantity;

            sb.append(
                    rank
                            + ". "
                            + p.name
                            + "  | ID: "
                            + p.id
                            + "  | Current: "
                            + p.quantity
                            + "  | Reorder: "
                            + p.reorderLevel
                            + "  | Shortage: "
                            + shortage
                            + "\n"
            );

            rank++;
        }

        if (rank == 1) {

            sb.append(
                    "No products currently require restocking."
            );
        }

        output.setText(
                sb.toString()
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // BST PRICE ORDER
    // =========================================================

    static void showBST() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "BST Price Order",
                        "Binary Search Tree traversal arranged by product price"
                ),
                BorderLayout.NORTH
        );

        PriceBST bst =
                new PriceBST();

        for (Product p : getAllProducts()) {

            bst.insert(p);
        }

        ArrayList<Product> result =
                new ArrayList<>();

        bst.inorder(
                bst.root,
                result
        );

        JTextArea output =
                createTextArea();

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "PRODUCTS BY PRICE\n"
                        + "============================\n\n"
        );

        for (Product p : result) {

            sb.append(
                    p.name
                            + "  |  "
                            + String.format(
                            "₹%.2f",
                            p.price
                    )
                            + "  |  ID: "
                            + p.id
                            + "\n"
            );
        }

        output.setText(
                sb.toString()
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    static void showStatistics() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Inventory Statistics",
                        "Analyze inventory size, quantity, value and stock health"
                ),
                BorderLayout.NORTH
        );

        ArrayList<Product> products =
                getAllProducts();

        int totalProducts =
                products.size();

        int totalUnits = 0;

        int lowStock = 0;

        double value = 0;

        Product highestValue =
                null;

        Product highestQuantity =
                null;

        for (Product p : products) {

            totalUnits += p.quantity;

            value +=
                    p.price * p.quantity;

            if (p.isLowStock()) {
                lowStock++;
            }

            if (highestValue == null
                    || p.price * p.quantity
                    > highestValue.price
                    * highestValue.quantity) {

                highestValue = p;
            }

            if (highestQuantity == null
                    || p.quantity
                    > highestQuantity.quantity) {

                highestQuantity = p;
            }
        }

        JTextArea output =
                createTextArea();

        output.setText(
                "INVENTORY STATISTICS\n"
                        + "============================\n\n"
                        + "Total Products      : "
                        + totalProducts
                        + "\n"
                        + "Total Units         : "
                        + totalUnits
                        + "\n"
                        + "Low Stock Products  : "
                        + lowStock
                        + "\n"
                        + "Inventory Value     : "
                        + String.format(
                        "₹%.2f",
                        value
                )
                        + "\n\n"
                        + "Highest Stock Product\n"
                        + "----------------------------\n"
                        + (highestQuantity == null
                        ? "None"
                        : highestQuantity.name
                        + " ("
                        + highestQuantity.quantity
                        + " units)")
                        + "\n\n"
                        + "Highest Inventory Value\n"
                        + "----------------------------\n"
                        + (highestValue == null
                        ? "None"
                        : highestValue.name
                        + " ("
                        + String.format(
                        "₹%.2f",
                        highestValue.price
                                * highestValue.quantity
                )
                        + ")")
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // TRANSACTIONS
    // =========================================================

    static void showTransactions() {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        "Transactions",
                        "Recent inventory operations stored using queue concepts"
                ),
                BorderLayout.NORTH
        );

        JTextArea output =
                createTextArea();

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "TRANSACTION HISTORY\n"
                        + "============================\n\n"
        );

        if (transactions.isEmpty()) {

            sb.append(
                    "No transactions recorded yet."
            );

        } else {

            for (int i = 0;
                 i < transactions.size();
                 i++) {

                Transaction t =
                        transactions.get(i);

                sb.append(
                        (i + 1)
                                + ". "
                                + t.type
                                + " | Product ID: "
                                + t.productId
                                + " | "
                                + t.description
                                + "\n"
                );
            }
        }

        output.setText(
                sb.toString()
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // SORTING
    // =========================================================

    static void sortByName() {

        ArrayList<Product> products =
                getAllProducts();

        products.sort(
                Comparator.comparing(
                        p -> p.name
                )
        );

        showSortedProducts(
                "Products Sorted by Name",
                products
        );
    }

    static void sortByQuantity() {

        ArrayList<Product> products =
                getAllProducts();

        products.sort(
                Comparator.comparingInt(
                        p -> p.quantity
                )
        );

        showSortedProducts(
                "Products Sorted by Quantity",
                products
        );
    }

    static void sortByPrice() {

        ArrayList<Product> products =
                getAllProducts();

        products.sort(
                Comparator.comparingDouble(
                        p -> p.price
                )
        );

        showSortedProducts(
                "Products Sorted by Price",
                products
        );
    }

    static void showSortedProducts(
            String title,
            ArrayList<Product> products
    ) {

        contentPanel.removeAll();

        JPanel main =
                new JPanel(
                        new BorderLayout()
                );

        main.setBackground(BACKGROUND);

        main.add(
                createHeader(
                        title,
                        "Products arranged using sorting algorithms"
                ),
                BorderLayout.NORTH
        );

        JTextArea output =
                createTextArea();

        StringBuilder sb =
                new StringBuilder();

        for (Product p : products) {

            sb.append(
                    p.id
                            + " | "
                            + p.name
                            + " | "
                            + p.category
                            + " | ₹"
                            + p.price
                            + " | Qty: "
                            + p.quantity
                            + "\n"
            );
        }

        output.setText(
                sb.toString()
        );

        main.add(
                createOutputScroll(output),
                BorderLayout.CENTER
        );

        contentPanel.add(main);

        refreshContent();
    }

    // =========================================================
    // ADD PRODUCT LOGIC
    // =========================================================

    static void addProduct() {

        try {

            int id =
                    Integer.parseInt(
                            idField.getText().trim()
                    );

            String name =
                    nameField.getText().trim();

            String category =
                    categoryField.getText().trim();

            double price =
                    Double.parseDouble(
                            priceField.getText().trim()
                    );

            int quantity =
                    Integer.parseInt(
                            quantityField.getText().trim()
                    );

            int reorder =
                    Integer.parseInt(
                            reorderField.getText().trim()
                    );

            if (name.isEmpty()
                    || category.isEmpty()) {

                setStatus(
                        "Please fill all fields"
                );

                return;
            }

            if (price < 0
                    || quantity < 0
                    || reorder < 0) {

                setStatus(
                        "Values cannot be negative"
                );

                return;
            }

            if (inventory.get(id) != null) {

                setStatus(
                        "Product ID already exists"
                );

                return;
            }

            Product product =
                    new Product(
                            id,
                            name,
                            category,
                            price,
                            quantity,
                            reorder
                    );

            inventory.put(product);

            transactions.add(
                    new Transaction(
                            "ADD",
                            id,
                            "Added " + name
                    )
            );

            setStatus(
                    "Product added successfully"
            );

            JOptionPane.showMessageDialog(
                    frame,
                    "Product added successfully!"
            );

            clearFields();

        } catch (Exception ex) {

            setStatus(
                    "Please enter valid values"
            );
        }
    }

    // =========================================================
    // FORM HELPERS
    // =========================================================

    static JPanel createFormPanel() {

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                0,
                                2,
                                15,
                                15
                        )
                );

        panel.setBackground(BACKGROUND);

        panel.setBorder(
                new EmptyBorder(
                        10,
                        0,
                        10,
                        0
                )
        );

        return panel;
    }

    static void addFormRow(
            JPanel panel,
            String label,
            JTextField field
    ) {

        JLabel l =
                new JLabel(label);

        l.setForeground(TEXT);

        l.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        field.setBackground(CARD2);

        field.setForeground(TEXT);

        field.setCaretColor(TEXT);

        field.setBorder(
                new EmptyBorder(
                        8,
                        10,
                        8,
                        10
                )
        );

        panel.add(l);

        panel.add(field);
    }

    static JButton createActionButton(
            String text,
            Color color
    ) {

        JButton button =
                new JButton(text);

        button.setForeground(TEXT);

        button.setBackground(color);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        button.setFocusPainted(false);

        button.setBorder(
                new EmptyBorder(
                        10,
                        18,
                        10,
                        18
                )
        );

        return button;
    }

    static JTextArea createTextArea() {

        JTextArea area =
                new JTextArea();

        area.setBackground(CARD);

        area.setForeground(TEXT);

        area.setCaretColor(TEXT);

        area.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14
                )
        );

        area.setEditable(false);

        area.setBorder(
                new EmptyBorder(
                        20,
                        20,
                        20,
                        20
                )
        );

        return area;
    }

    static JScrollPane createOutputScroll(
            JTextArea area
    ) {

        JScrollPane scroll =
                new JScrollPane(area);

        scroll.setBorder(null);

        scroll.getViewport().setBackground(CARD);

        return scroll;
    }

    // =========================================================
    // PRODUCT DETAILS
    // =========================================================

    static String productDetails(
            Product p
    ) {

        return "Product ID     : "
                + p.id
                + "\n"
                + "Product Name   : "
                + p.name
                + "\n"
                + "Category       : "
                + p.category
                + "\n"
                + "Price          : "
                + String.format(
                "₹%.2f",
                p.price
        )
                + "\n"
                + "Quantity       : "
                + p.quantity
                + "\n"
                + "Reorder Level  : "
                + p.reorderLevel
                + "\n"
                + "Status         : "
                + (p.isLowStock()
                ? "LOW STOCK"
                : "IN STOCK");
    }

    // =========================================================
    // CLEAR FIELDS
    // =========================================================

    static void clearFields() {

        if (idField != null) {
            idField.setText("");
        }

        if (nameField != null) {
            nameField.setText("");
        }

        if (categoryField != null) {
            categoryField.setText("");
        }

        if (priceField != null) {
            priceField.setText("");
        }

        if (quantityField != null) {
            quantityField.setText("");
        }

        if (reorderField != null) {
            reorderField.setText("");
        }

        if (searchField != null) {
            searchField.setText("");
        }
    }

    // =========================================================
    // REFRESH
    // =========================================================

    static void refreshContent() {

        contentPanel.revalidate();

        contentPanel.repaint();

        updateDashboard();

        refreshTable();
    }

    // =========================================================
    // STATUS
    // =========================================================

    static void setStatus(
            String message
    ) {

        if (statusLabel != null) {

            statusLabel.setText(message);
        }
    }
}