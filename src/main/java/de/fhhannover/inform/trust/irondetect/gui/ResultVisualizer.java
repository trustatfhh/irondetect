package de.fhhannover.inform.trust.irondetect.gui;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover 
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of irongui, version 0.0.3, implemented by the Trust@FHH 
 * research group at the Hochschule Hannover, a program to visualize the content
 * of a MAP Server (MAPS), a crucial component within the TNC architecture.
 * 
 * The development was started within the bachelor
 * thesis of Tobias Ruhe at Hochschule Hannover (University of
 * Applied Sciences and Arts Hannover). irongui is now maintained
 * and extended within the ESUKOM research project. More information
 * can be found at the Trust@FHH website.
 * %%
 * Copyright (C) 2010 - 2013 Trust@FHH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.irondetect.util.event.Event;
import de.fhhannover.inform.trust.irondetect.util.event.EventReceiver;
import de.fhhannover.inform.trust.irondetect.util.event.EventType;
import de.fhhannover.inform.trust.irondetect.util.event.ResultUpdateEvent;

public class ResultVisualizer implements EventReceiver {

	class ResultTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -3297226482363924926L;

		private Vector<ResultObject> resultObjects;
		
		public ResultTableModel() {
			this.resultObjects = new Vector<ResultObject>();
			
		}
		
		public void addRow(ResultObject ro) {
			ro.setIndex(this.getRowCount() + 1);
			this.resultObjects.add(0, ro);	// insert at FIRST index in vector
			fireTableDataChanged();
		}
		
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "#";
			case 1:
				return "Device";
			case 2:
				return "ID";
			case 3:
				return "Value";
			case 4:
				return "Timestamp";
			default:
				return "";
			}
	    }
		
		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class<? extends Object> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return this.resultObjects.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			ResultObject tmp = this.resultObjects.get(row);
			switch (col) {
			case 0:
				return tmp.getIndex();
			case 1:
				return tmp.getDevice();
			case 2:
				return tmp.getId();
			case 3:
				return tmp.getValue();
			case 4:
				return tmp.getTimeStamp();
			default:
				return null;
			}
		}
	}

	public class BooleanRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = -8630665605182392700L;
		private boolean isBordered;

		public BooleanRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (value instanceof Boolean) {
				boolean b = (Boolean) value;
				if (b) {
					setBackground(Color.RED);	// true is red
				} else {
					setBackground(Color.GREEN);	// false is green
					
				}
				
				if (isBordered) {
					setBorder(BorderFactory.createMatteBorder(2,5,2,5,
							table.getBackground()));
				}
			} else if (value instanceof String) {
				setText((String) value);
			}
			
			return this;
		}

	}
	
	private static final int RULE = 0;
	private static final int SIGNATURE = 1;
	private static final int ANOMALY = 2;
	private static final int CONDITION = 3;
	private static final String[] TYPES = new String[]{"Rules", "Signatures", "Anomalies", "Conditions"};

	private Logger logger = Logger.getLogger(ResultVisualizer.class);

	private JTable[] tables;

	private JScrollPane[] scrollPanes;

	private ResultTableModel[] tableModels;

	private JPanel[] panels;
	private Dimension screenSize;

	public ResultVisualizer() {
		this.tables = new JTable[4];
		this.scrollPanes = new JScrollPane[4];
		this.tableModels = new ResultTableModel[4];
		this.panels = new JPanel[4];
		
		this.screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		initComponentsForType(RULE);
		initComponentsForType(SIGNATURE);
		initComponentsForType(ANOMALY);
		initComponentsForType(CONDITION);

		logger.info(ResultVisualizer.class.getSimpleName() + " has started.");
	}

	private void createAndShowGUI(int type) {
		// Create and set up the window.
		JFrame frame = new JFrame("irondetect - " + TYPES[type]);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setMinimumSize(new Dimension((this.screenSize.width / 2) - (this.screenSize.width * 2 / 100), (this.screenSize.height / 2) - (this.screenSize.height * 2 / 100)));
		
		switch (type) {
		case RULE:
			frame.setLocation(0, 0);
			break;
		case SIGNATURE:
			frame.setLocation(this.screenSize.width / 2, 0);
			break;
		case ANOMALY:
			frame.setLocation(0, this.screenSize.height / 2);
			break;
		case CONDITION:
			frame.setLocation(this.screenSize.width / 2, this.screenSize.height / 2);
			break;
		default:
			break;
		}

		// Create and set up the content pane.
		this.panels[type].setOpaque(true); // content panes must be opaque
		frame.setContentPane(this.panels[type]);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private void initComponentsForType(final int type) {
		this.panels[type] = new JPanel();
		this.tableModels[type] = new ResultTableModel();
		this.tables[type] = new JTable(this.tableModels[type]);
		this.tables[type].setEnabled(false);
//		this.tables[type].setDefaultRenderer(Boolean.class, new BooleanRenderer(true));
		this.scrollPanes[type] = new JScrollPane(this.tables[type]);
		this.tables[type].setFillsViewportHeight(true);
		this.tables[type].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int tableSize = this.screenSize.width / 2;
		this.tables[type].getColumnModel().getColumn(0).setPreferredWidth(tableSize * 10 / 100);
		this.tables[type].getColumnModel().getColumn(1).setPreferredWidth(tableSize * 15 / 100);
		this.tables[type].getColumnModel().getColumn(2).setPreferredWidth(tableSize * 25 / 100);
		this.tables[type].getColumnModel().getColumn(3).setPreferredWidth(tableSize * 20 / 100);
		this.tables[type].getColumnModel().getColumn(4).setPreferredWidth(tableSize * 30 / 100);
		this.panels[type].setLayout(new BorderLayout());
		this.panels[type].add(this.scrollPanes[type], BorderLayout.CENTER);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(type);
			}
		});
	}

	private int typeToInt(String type) {
		if (type.equalsIgnoreCase("rule")) {
			return RULE;
		} else if (type.equalsIgnoreCase("signature")) {
			return SIGNATURE;
		} else if (type.equalsIgnoreCase("anomaly")) {
			return ANOMALY;
		} else if (type.equalsIgnoreCase("condition")) {
			return CONDITION;
		} else {
			return -1;
		}
	}
	
	@Override
	public void submitNewEvent(Event e) {
		if (e.getType() == EventType.RESULT_UPDATE) {
			ResultObject ro = ((ResultUpdateEvent) e).getPayload();
			logger.trace("Received result update: device==" + ro.getDevice()
					+ ", type==" + ro.getType() + ", id==" + ro.getId()
					+ ", value==" + ro.getValue() + ", timestamp==" + ro.getTimeStamp());

			final int type = typeToInt(ro.getType());
			this.tableModels[type].addRow(ro);
			this.scrollPanes[type].revalidate();
		}
	}
}
