 import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

// 1. Abstraction: abstract base class for everything stored in the library
abstract class LibraryItem {
protected String id, title;

public LibraryItem(String id, String title) {
this.id = id;
this.title = title;
}

public String getId() { return id; }
public String getTitle() { return title; }

// 2. Polymorphism: abstract methods, each subclass gives its own version
public abstract Object[] toRow();          // one row of the JTable
public abstract String toFileString();     // one line of books.txt
}

// Custom checked exception for wrong issue / return operations
class BookNotAvailableException extends Exception {
public BookNotAvailableException(String message) {
super(message);
}
}

// 3. Inheritance: Book extends LibraryItem
class Book extends LibraryItem {
private String author, category, issuedTo;
private boolean issued;

public Book(String id, String title, String author, String category) {
this(id, title, author, category, false, "-");
}

public Book(String id, String title, String author, String category,
boolean issued, String issuedTo) {
super(id, title);
this.author = author;
this.category = category;
this.issued = issued;
this.issuedTo = issuedTo;
}

// Encapsulation: private fields, access only through methods
public String getAuthor() { return author; }
public String getCategory() { return category; }
public boolean isIssued() { return issued; }
public String getIssuedTo() { return issuedTo; }

public void issue(String member) throws BookNotAvailableException {
if (issued) {
throw new BookNotAvailableException("Book already issued to " + issuedTo + ".");
}
issued = true;
issuedTo = member;
}

public void giveBack() throws BookNotAvailableException {
if (!issued) {
throw new BookNotAvailableException("This book is not issued to anyone.");
}
issued = false;
issuedTo = "-";
}

public boolean matches(String key) {
return id.toLowerCase().contains(key) || title.toLowerCase().contains(key)
|| author.toLowerCase().contains(key) || category.toLowerCase().contains(key);
}

// Method Overriding
@Override
public Object[] toRow() {
return new Object[] { id, title, author, category,
issued ? "Issued" : "Available", issuedTo };
}

@Override
public String toFileString() {
return String.join("|", id, title, author, category, String.valueOf(issued), issuedTo);
}

public static Book fromFileString(String line) {
String[] p = line.split("\\|", -1);
if (p.length != 6) return null;
return new Book(p[0], p[1], p[2], p[3], Boolean.parseBoolean(p[4]), p[5]);
}
}

// Main panel - Swing components + Event Handling
class LibraryPanel extends JPanel implements ActionListener {
private static final String FILE_NAME = "books.txt";
private static final String[] CATEGORIES = { "Programming", "Computer Science",
"Database", "Networking", "Biography", "Fiction", "Other" };

// 4. Collections Framework: ArrayList stores all the books
private ArrayList<Book> books = new ArrayList<>();

private JTextField tfId = new JTextField();
private JTextField tfTitle = new JTextField();
private JTextField tfAuthor = new JTextField();
private JComboBox<String> cbCategory = new JComboBox<>(CATEGORIES);
private JTextField tfMember = new JTextField();
private JTextField tfSearch = new JTextField(22);

private JButton btnAdd = makeButton("Add Book", new Color(39, 174, 96));
private JButton btnDelete = makeButton("Delete Book", new Color(192, 57, 43));
private JButton btnIssue = makeButton("Issue Book", new Color(41, 128, 185));
private JButton btnReturn = makeButton("Return Book", new Color(230, 126, 34));
private JButton btnClear = makeButton("Clear Fields", new Color(127, 140, 141));
private JButton btnSearch = makeButton("Search", new Color(41, 128, 185));
private JButton btnShowAll = makeButton("Show All", new Color(127, 140, 141));

private DefaultTableModel model;
private JTable table;
private JLabel lblStats = new JLabel();
private JLabel lblStatus = new JLabel(" ");

public LibraryPanel() {
setPreferredSize(new Dimension(1020, 580));
setLayout(new BorderLayout());

add(buildHeader(), BorderLayout.NORTH);
add(buildFormPanel(), BorderLayout.WEST);
add(buildTablePanel(), BorderLayout.CENTER);
add(buildStatusPanel(), BorderLayout.SOUTH);

loadBooks();
refreshTable(books);
setStatus("Welcome! " + books.size() + " book(s) loaded from " + FILE_NAME + ".", true);
}

private static JButton makeButton(String text, Color color) {
JButton b = new JButton(text);
b.setBackground(color);
b.setForeground(Color.WHITE);
b.setFont(new Font("SansSerif", Font.BOLD, 13));
b.setFocusPainted(false);
b.setOpaque(true);
return b;
}

private JPanel buildHeader() {
JPanel p = new JPanel(new BorderLayout());
p.setBackground(new Color(31, 56, 100));
p.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
JLabel title = new JLabel("Library Management System");
title.setFont(new Font("Serif", Font.BOLD, 26));
title.setForeground(Color.WHITE);
lblStats.setFont(new Font("SansSerif", Font.BOLD, 14));
lblStats.setForeground(new Color(255, 230, 120));
p.add(title, BorderLayout.WEST);
p.add(lblStats, BorderLayout.EAST);
return p;
}

private JPanel buildFormPanel() {
JPanel p = new JPanel(new GridBagLayout());
p.setPreferredSize(new Dimension(290, 0));
p.setBorder(BorderFactory.createTitledBorder("Book Details"));
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.HORIZONTAL;
c.insets = new Insets(4, 8, 4, 8);
c.gridx = 0;
c.weightx = 1;

String[] labels = { "Book ID", "Title", "Author", "Category", "Member Name" };
JComponent[] fields = { tfId, tfTitle, tfAuthor, cbCategory, tfMember };
int row = 0;
for (int i = 0; i < labels.length; i++) {
c.gridy = row++;
p.add(new JLabel(labels[i]), c);
c.gridy = row++;
p.add(fields[i], c);
}

JPanel r1 = new JPanel(new GridLayout(1, 2, 6, 0));
r1.add(btnAdd);
r1.add(btnDelete);
JPanel r2 = new JPanel(new GridLayout(1, 2, 6, 0));
r2.add(btnIssue);
r2.add(btnReturn);
c.gridy = row++; c.insets = new Insets(14, 8, 4, 8);
p.add(r1, c);
c.gridy = row++; c.insets = new Insets(4, 8, 4, 8);
p.add(r2, c);
c.gridy = row++;
p.add(btnClear, c);

c.gridy = row; c.weighty = 1;               // push everything to the top
p.add(Box.createVerticalGlue(), c);

btnAdd.addActionListener(this);
btnDelete.addActionListener(this);
btnIssue.addActionListener(this);
btnReturn.addActionListener(this);
btnClear.addActionListener(this);
return p;
}

private JPanel buildTablePanel() {
JPanel p = new JPanel(new BorderLayout());

JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
search.add(new JLabel("Search:"));
search.add(tfSearch);
search.add(btnSearch);
search.add(btnShowAll);
btnSearch.addActionListener(this);
btnShowAll.addActionListener(this);
tfSearch.addActionListener(this);           // Enter key also searches

String[] cols = { "ID", "Title", "Author", "Category", "Status", "Issued To" };
model = new DefaultTableModel(cols, 0) {
@Override
public boolean isCellEditable(int r, int c) { return false; }
};
table = new JTable(model);
table.setRowHeight(26);
table.setFont(new Font("SansSerif", Font.PLAIN, 13));
table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
int[] widths = { 55, 230, 140, 125, 75, 105 };
for (int i = 0; i < widths.length; i++) {
table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
}

// Status column: green for Available, red for Issued
table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
@Override
public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
boolean focus, int r, int col) {
Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, col);
if (!sel) {
comp.setForeground("Issued".equals(v) ? new Color(192, 57, 43)
: new Color(30, 130, 76));
}
return comp;
}
});

// Clicking a row copies the book into the form
table.getSelectionModel().addListSelectionListener(e -> {
if (!e.getValueIsAdjusting()) {
Book b = getSelectedBook();
if (b != null) {
tfId.setText(b.getId());
tfTitle.setText(b.getTitle());
tfAuthor.setText(b.getAuthor());
cbCategory.setSelectedItem(b.getCategory());
}
}
});

p.add(search, BorderLayout.NORTH);
p.add(new JScrollPane(table), BorderLayout.CENTER);
return p;
}

private JPanel buildStatusPanel() {
JPanel p = new JPanel(new BorderLayout());
p.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
lblStatus.setFont(new Font("SansSerif", Font.BOLD, 13));
p.add(lblStatus, BorderLayout.WEST);
return p;
}

// 5. File Handling: all books are stored in books.txt
private void loadBooks() {
books.clear();
File f = new File(FILE_NAME);
if (!f.exists()) return;
try (BufferedReader br = new BufferedReader(new FileReader(f))) {
String line;
while ((line = br.readLine()) != null) {
Book b = Book.fromFileString(line);
if (b != null) books.add(b);
}
} catch (IOException e) {
setStatus("Error reading " + FILE_NAME, false);
}
}

private boolean saveBooks() {
try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {
for (LibraryItem item : books) {
bw.write(item.toFileString());          // Polymorphism
bw.newLine();
}
return true;
} catch (IOException e) {
setStatus("Error saving " + FILE_NAME, false);
return false;
}
}

private void refreshTable(ArrayList<Book> list) {
model.setRowCount(0);
for (LibraryItem item : list) {
model.addRow(item.toRow());                 // Polymorphism
}
int issued = 0;
for (Book b : books) if (b.isIssued()) issued++;
lblStats.setText("Total: " + books.size() + "    Available: "
+ (books.size() - issued) + "    Issued: " + issued);
}

private Book findBook(String id) {
for (Book b : books) {
if (b.getId().equalsIgnoreCase(id)) return b;
}
return null;
}

private Book getSelectedBook() {
int row = table.getSelectedRow();
if (row < 0) return null;
return findBook((String) model.getValueAt(row, 0));
}

private void selectRow(String id) {
for (int i = 0; i < model.getRowCount(); i++) {
if (model.getValueAt(i, 0).equals(id)) {
table.setRowSelectionInterval(i, i);
return;
}
}
}

private void setStatus(String msg, boolean ok) {
lblStatus.setForeground(ok ? new Color(30, 130, 76) : new Color(192, 57, 43));
lblStatus.setText(msg);
}

private void clearFields() {
tfId.setText("");
tfTitle.setText("");
tfAuthor.setText("");
tfMember.setText("");
cbCategory.setSelectedIndex(0);
table.clearSelection();
}

private String clean(String s) {
return s.trim().replace("|", "/");              // '|' is the file separator
}

private String validMemberName() {
String member = clean(tfMember.getText());
if (member.isEmpty()) {
setStatus("Enter the Member Name first.", false);
return null;
}
if (!member.matches("[\\p{L}][\\p{L} .'-]*")) {
setStatus("Member Name may contain only letters, spaces, apostrophe or hyphen.", false);
return null;
}
return member;
}

private void addBook() {
String id = clean(tfId.getText());
String title = clean(tfTitle.getText());
String author = clean(tfAuthor.getText());
if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
setStatus("Please fill Book ID, Title and Author.", false);
return;
}
if (findBook(id) != null) {
setStatus("Book ID " + id + " already exists.", false);
return;
}
books.add(new Book(id, title, author, (String) cbCategory.getSelectedItem()));
if (!saveBooks()) {
books.remove(books.size() - 1);
return;
}
refreshTable(books);
clearFields();
setStatus("Book '" + title + "' added successfully.", true);
}

private void issueBook() {
Book b = getSelectedBook();
if (b == null) {
setStatus("Select a book from the table first.", false);
return;
}
String member = validMemberName();
if (member == null) {
return;
}
try {
b.issue(member);
if (!saveBooks()) {
try {
b.giveBack();
} catch (BookNotAvailableException ignored) {
}
return;
}
refreshTable(books);
selectRow(b.getId());
setStatus("'" + b.getTitle() + "' issued to " + member + ".", true);
} catch (BookNotAvailableException ex) {
setStatus(ex.getMessage(), false);
}
}

private void returnBook() {
Book b = getSelectedBook();
if (b == null) {
setStatus("Select a book from the table first.", false);
return;
}
String member = validMemberName();
if (member == null) {
return;
}
if (!b.isIssued()) {
setStatus("This book is not currently issued.", false);
return;
}
if (!b.getIssuedTo().equalsIgnoreCase(member)) {
setStatus("Return denied. This book is issued to " + b.getIssuedTo() + ".", false);
return;
}
try {
b.giveBack();
if (!saveBooks()) {
return;
}
refreshTable(books);
selectRow(b.getId());
setStatus("'" + b.getTitle() + "' returned successfully.", true);
} catch (BookNotAvailableException ex) {
setStatus(ex.getMessage(), false);
}
}

private void deleteBook() {
Book b = getSelectedBook();
if (b == null) {
setStatus("Select a book from the table first.", false);
return;
}
if (b.isIssued()) {
setStatus("Cannot delete an issued book. Return it to " + b.getIssuedTo() + " first.", false);
return;
}
books.remove(b);
if (!saveBooks()) {
books.add(b);
return;
}
refreshTable(books);
clearFields();
setStatus("Book '" + b.getTitle() + "' deleted.", true);
}

private void searchBooks() {
String key = tfSearch.getText().trim().toLowerCase();
if (key.isEmpty()) {
refreshTable(books);
setStatus("Showing all " + books.size() + " book(s).", true);
return;
}
ArrayList<Book> result = new ArrayList<>();
for (Book b : books) {
if (b.matches(key)) result.add(b);
}
refreshTable(result);
setStatus(result.size() + " book(s) found for \"" + key + "\".", result.size() > 0);
}

@Override
public void actionPerformed(ActionEvent e) {
Object src = e.getSource();
if (src == btnAdd) addBook();
else if (src == btnDelete) deleteBook();
else if (src == btnIssue) issueBook();
else if (src == btnReturn) returnBook();
else if (src == btnClear) { clearFields(); setStatus("Fields cleared.", true); }
else if (src == btnSearch || src == tfSearch) searchBooks();
else if (src == btnShowAll) {
tfSearch.setText("");
refreshTable(books);
setStatus("Showing all " + books.size() + " book(s).", true);
}
}
}

public class LibraryManagementSystem {
public static void main(String[] args) {
SwingUtilities.invokeLater(() -> {
JFrame frame = new JFrame("Library Management System using Java");
frame.add(new LibraryPanel());
frame.pack();
frame.setResizable(false);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setLocationRelativeTo(null);
frame.setVisible(true);
});
}
}
