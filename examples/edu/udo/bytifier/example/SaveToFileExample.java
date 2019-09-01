package edu.udo.bytifier.example;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;

import edu.udo.bytifier.Bytifier;
import edu.udo.bytifier.ProtocolBuilder;
import edu.udo.bytifier.protocols.ArrayListProtocol;
import net.miginfocom.swing.MigLayout;

public class SaveToFileExample {
	
	public static void main(String[] args) {
		EventQueue.invokeLater(ContactsApp::new);
	}
	
	static final Bytifier BYTIFIER;
	static {
//		ProtocolBuilder protocolBuilder = ;
		BYTIFIER = new ProtocolBuilder()
			.setStringEncodingCharset(StandardCharsets.UTF_8)
			.defineForClass(ArrayList.class, new ArrayListProtocol())
			.defineForClass(Contact.class)
				.addFieldString(Contact::getNicName, Contact::setNicName)
				.addFieldString(Contact::getFirstName, Contact::setFirstName)
				.addFieldString(Contact::getLastName, Contact::setLastName)
				.addFieldString(Contact::getAdress, Contact::setAdress)
				.addFieldString(Contact::getHomeNumber, Contact::setHomeNumber)
				.addFieldString(Contact::getMobileNumber, Contact::setMobileNumber)
				.endDefinition()
			.defineForClass(ContactList.class)
				.addFieldValueObject(ContactList::getContacts, ContactList::setContacts)
				.endDefinition()
			.build();
//		protocolBuilder.defineForClass(ContactList.class, new ClassProtocol() {
//			@Override
//			public void write(Bytifier bytifier, EncodeData data, Object input) {
//				ContactList cl = (ContactList) input;
//				data.writeInt4(cl.contacts.size());
//				for (int i = 0; i < cl.contacts.size(); i++) {
//					bytifier.writeChunk(data, cl.contacts.get(i), true);
//				}
//			}
//			@Override
//			public void read(Bytifier bytifier, DecodeData data, Object object) {
//				ContactList cl = (ContactList) object;
//				cl.contacts.clear();
//				for (int i = 0; i < data.readInt4(); i++) {
//					cl.contacts.add((Contact) bytifier.readChunk(data));
//				}
//			}
//			@Override
//			public Object create(Bytifier bytifier, DecodeData data) {
//				return new ContactList();
//			}
//		});
//		BYTIFIER = protocolBuilder.build();
//		BYTIFIER = null;
	}
	
	static class ContactsApp {
		
		final JFrame frame;
		final JList<String> listView;
		final JTextField inputNic = new JTextField();
		final JTextField inputFN = new JTextField();
		final JTextField inputLN = new JTextField();
		final JTextField inputAdr = new JTextField();
		final JTextField inputHN = new JTextField();
		final JTextField inputMN = new JTextField();
		final JButton btnSave = new JButton("Save");
		
		ContactList contactList = new ContactList();
		Contact selectedContact;
		
		public ContactsApp() {
			frame = new JFrame("Contacts App");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(640, 480);
			frame.setLocationRelativeTo(null);
			
			JMenuItem itemNew = new JMenuItem("New");
			itemNew.addActionListener(this::onNew);
			
			JMenuItem itemOpen = new JMenuItem("Open");
			itemOpen.addActionListener(this::onOpen);
			
			JMenuItem itemSave = new JMenuItem("Save");
			itemSave.addActionListener(this::onSave);
			
			JMenuItem itemExit = new JMenuItem("Exit");
			itemExit.addActionListener(this::onExit);
			
			JMenu menuFile = new JMenu("File");
			menuFile.add(itemNew);
			menuFile.add(itemOpen);
			menuFile.add(itemSave);
			menuFile.add(itemExit);
			
			JMenuBar menuBar = new JMenuBar();
			frame.setJMenuBar(menuBar);
			menuBar.add(menuFile);
			
			JPanel editor = new JPanel();
			editor.setLayout(new MigLayout("", "[grow][grow]", "[][][][][][][][][]"));
			
			editor.add(new JLabel("Nic Name"), "cell 0 0");
			editor.add(new JLabel("First Name"), "cell 0 2");
			editor.add(new JLabel("Last Name"), "cell 1 2");
			editor.add(new JLabel("Adress"), "cell 0 4");
			editor.add(new JLabel("Home Number"), "cell 0 6");
			editor.add(new JLabel("Mobile Number"), "cell 1 6");
			editor.add(inputNic, "cell 0 1 2 1,growx");
			editor.add(inputFN, "cell 0 3,growx");
			editor.add(inputLN, "cell 1 3,growx");
			editor.add(inputAdr, "cell 0 5 2 1,growx");
			editor.add(inputHN, "cell 0 7,growx");
			editor.add(inputMN, "cell 1 7,growx");
			
			btnSave.addActionListener(this::onSaveClick);
			editor.add(btnSave, "cell 0 8 2 1,alignx center");
			
			listView = new JList<>();
			listView.setMinimumSize(new Dimension(128, 128));
			listView.addListSelectionListener(this::onContactSelected);
			JSplitPane body = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(listView), editor);
			frame.setContentPane(body);
			
			refreshContactList();
			
			frame.setVisible(true);
		}
		
		private void refreshContactList() {
			listView.removeAll();
			DefaultListModel<String> model = new DefaultListModel<>();
			for (int i = 0; i < contactList.getCount(); i++) {
				Contact con = contactList.getByIndex(i);
				model.addElement(con.getVisibleName());
			}
			listView.setModel(model);
		}
		
		private void onContactSelected(ListSelectionEvent e) {
			int index = listView.getSelectedIndex();
			boolean hasSelection = index != -1;
//			inputNic.setEnabled(hasSelection);
//			inputFN.setEnabled(hasSelection);
//			inputLN.setEnabled(hasSelection);
//			inputAdr.setEnabled(hasSelection);
//			inputHN.setEnabled(hasSelection);
//			inputMN.setEnabled(hasSelection);
//			btnSave.setEnabled(hasSelection);
			selectedContact = hasSelection ? contactList.getByIndex(index) : null;
			
			if (selectedContact == null) {
				return;
			}
			inputNic.setText(selectedContact.nicName);
			inputFN.setText(selectedContact.firstName);
			inputLN.setText(selectedContact.lastName);
			inputAdr.setText(selectedContact.adress);
			inputHN.setText(selectedContact.homeNumber);
			inputMN.setText(selectedContact.mobileNumber);
		}
		
		private void onSaveClick(ActionEvent e) {
			if (selectedContact == null) {
				selectedContact = contactList.addContact("", "New", "Contact");
				DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
				model.addElement(selectedContact.getVisibleName());
			}
			selectedContact.nicName = inputNic.getText();
			selectedContact.firstName = inputFN.getText();
			selectedContact.lastName = inputLN.getText();
			selectedContact.adress = inputAdr.getText();
			selectedContact.homeNumber = inputHN.getText();
			selectedContact.mobileNumber = inputMN.getText();
			
			int index = contactList.getIndexOf(selectedContact);
			DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
			model.set(index, selectedContact.getVisibleName());
		}
		
		private void onNew(ActionEvent e) {
			contactList.clear();
			contactList.addContact("Anna", "Anna", "A.");
			contactList.addContact("BB", "Benjamin", "B.");
			contactList.addContact("Cici", "Cynthia", "C.");
			refreshContactList();
		}
		
		private void onOpen(ActionEvent e) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			int result = fc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				try {
					byte[] bytes = Files.readAllBytes(file.toPath());
					contactList = (ContactList) BYTIFIER.decode(bytes);
					refreshContactList();
				} catch (Exception e1) {
					StringWriter sw = new StringWriter();
					e1.printStackTrace(new PrintWriter(sw));
					JOptionPane.showMessageDialog(frame,
							"An error occured while reading the file:\n"+sw.toString(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		private void onSave(ActionEvent e) {
			JFileChooser fc = new JFileChooser(".");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setMultiSelectionEnabled(false);
			int result = fc.showSaveDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				
				byte[] bytes = BYTIFIER.encode(contactList);
				
				try (FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(bytes);
				} catch (Exception e1) {
					StringWriter sw = new StringWriter();
					e1.printStackTrace(new PrintWriter(sw));
					JOptionPane.showMessageDialog(frame,
							"An error occured while saving the file:\n"+sw.toString(),
							"Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		private void onExit(ActionEvent e) {
			frame.dispose();
		}
		
	}
	
	static class ContactList {
		
		List<Contact> contacts = new ArrayList<>();
		
		public List<Contact> getContacts() {
			return contacts;
		}
		
		public void setContacts(List<Contact> contacts) {
			this.contacts = contacts;
		}
		
		public Contact addContact(String nic, String first, String last) {
			Contact con = new Contact();
			con.nicName = nic;
			con.firstName = first;
			con.lastName = last;
			contacts.add(con);
			return con;
		}
		
		public void clear() {
			contacts.clear();
		}
		
		public int getCount() {
			return contacts.size();
		}
		
		public int getIndexOf(Contact contact) {
			return contacts.indexOf(contact);
		}
		
		public Contact getByIndex(int index) {
			return contacts.get(index);
		}
		
		public Contact findByName(String name) {
			return contacts.stream()
					.filter(c -> isMatch(c.getNames(), name))
					.findFirst()
					.orElse(null);
		}
		
		public Contact findByNumber(String name) {
			return contacts.stream()
					.filter(c -> isMatch(c.getNumbers(), name))
					.findFirst()
					.orElse(null);
		}
		
		public List<Contact> findAllContainingPattern(String pattern) {
			return contacts.stream()
					.filter(c -> isMatch(c.getAllFields(), pattern))
					.collect(Collectors.toList());
		}
		
		private boolean isMatch(String[] strings, String pattern) {
			for (String s : strings) {
				if (s.contains(pattern) || pattern.contains(s)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	static class Contact {
		
		String nicName = "";
		String firstName = "", lastName = "", adress = "";
		String homeNumber = "", mobileNumber = "";
		
		public String getVisibleName() {
			for (String str : getNames()) {
				if (str != null && str.length() > 0) {
					return str;
				}
			}
			return "<no name>";
		}
		
		public String[] getNames() {
			return new String[] {nicName, firstName, lastName};
		}
		
		public String[] getNumbers() {
			return new String[] {homeNumber, mobileNumber};
		}
		
		public String[] getAllFields() {
			return new String[] {nicName, firstName, lastName, adress, homeNumber, mobileNumber};
		}
		public String getNicName() {
			return nicName;
		}
		
		public void setNicName(String nicName) {
			this.nicName = nicName;
		}
		
		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		public String getAdress() {
			return adress;
		}
		
		public void setAdress(String adress) {
			this.adress = adress;
		}
		
		public String getHomeNumber() {
			return homeNumber;
		}
		
		public void setHomeNumber(String homeNumber) {
			this.homeNumber = homeNumber;
		}
		
		public String getMobileNumber() {
			return mobileNumber;
		}
		
		public void setMobileNumber(String mobileNumber) {
			this.mobileNumber = mobileNumber;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{nic=");
			sb.append(nicName);
			sb.append("; first name=");
			sb.append(firstName);
			sb.append("; last name=");
			sb.append(lastName);
			sb.append("; adress=");
			sb.append(adress);
			sb.append("; home=");
			sb.append(homeNumber);
			sb.append("; mobile=");
			sb.append(mobileNumber);
			sb.append("}");
			return sb.toString();
		}
	}
	
}