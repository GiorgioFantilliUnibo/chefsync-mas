package view;

import jason.environment.grid.GridWorldView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.SwingUtilities;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.*;

/**
 * Concrete implementation of the {@link KitchenView} interface using Jason's {@link GridWorldView}.
 */
public class KitchenViewImpl extends GridWorldView implements KitchenView {
    
    private KitchenModelImpl kitchenModel;
    private DefaultTableModel ordersTableModel;
    private JTable ordersTable;


    public KitchenViewImpl(KitchenModelImpl model) {
        super(model, "ChefSync - Kitchen Monitor", 700);
        this.kitchenModel = model;
        
        setupDashboard();
        
        defaultFont = new Font("Arial", Font.BOLD, 12); 
        setVisible(true);
        repaint();
    }

    @Override
    public void updateView() {
        SwingUtilities.invokeLater(() -> {
            ordersTableModel.setRowCount(0);
            for (OrderRecord order : kitchenModel.getOrders()) {
                ordersTableModel.addRow(new Object[]{
                    "Order " + order.id(), order.dish(), order.status()
                });
                for (TaskRecord task : order.tasks()) {
                    String taskStatus;
                    if (task.completed()) {
                        taskStatus = "DONE";
                    } else if (task.assignedTo() != null) {
                        taskStatus = "-> " + task.assignedTo();
                    } else {
                        taskStatus = "WAITING";
                    }
                    ordersTableModel.addRow(new Object[]{"", "  " + task.name(), taskStatus});
                }
            }
            this.repaint();
        });
    }

    @Override
    public void drawObstacle(Graphics g, int x, int y) {
        Workstation ws = kitchenModel.getWorkstationAt(x, y);
        
        if (ws != null) {
            String owner = ws.getLockOwner();
            
            g.setColor(owner != null ? new Color(255, 100, 100) : new Color(100, 255, 100));
            int cellSizeW = getCanvas().getWidth() / KitchenModelImpl.GSize;
            int cellSizeH = getCanvas().getHeight() / KitchenModelImpl.GSize;
            g.fillRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
            
            g.setColor(Color.BLACK);
            String label = ws.getName().equals("prep_counter") ? "PREP" : ws.getName().toUpperCase();
            
            if (owner != null) {
                label += " (L: " + owner.replace("station_chef", "SC") + ")";
            }
            super.drawString(g, x, y, defaultFont, label);
            
            for (int i=0; i<10; i++) {
                jason.environment.grid.Location loc = kitchenModel.getAgPos(i);
                if (loc != null && loc.x == x && loc.y == y) {
                    drawAgent(g, x, y, Color.BLUE, i);
                }
            }
        } else {
            super.drawObstacle(g, x, y);
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        String name = kitchenModel.getAgentName(id);
        
        if (name == null || !name.startsWith("station_chef")) {
            return;
        }

        int cellSizeW = getCanvas().getWidth() / KitchenModelImpl.GSize;
        int cellSizeH = getCanvas().getHeight() / KitchenModelImpl.GSize;
        
        g.setColor(Color.BLUE);
        g.fillOval(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
        
        g.setColor(Color.WHITE);
        String shortName = name.replace("station_chef", "SC");
        super.drawString(g, x, y, defaultFont, shortName);
    }


    private void setupDashboard() {
        getContentPane().setLayout(new BorderLayout());
        
        getCanvas().setPreferredSize(new Dimension(700, 700));
        getContentPane().add(getCanvas(), BorderLayout.CENTER);
        
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(300, 700));
        sidePanel.setBorder(BorderFactory.createTitledBorder("Order & Task Management"));

        String[] columnNames = {"Order ID", "Dish", "Status"};
        ordersTableModel = new DefaultTableModel(columnNames, 0);
        ordersTable = new JTable(ordersTableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        
        sidePanel.add(new JLabel("Order History:"));
        sidePanel.add(scrollPane);
        
        getContentPane().add(sidePanel, BorderLayout.EAST);
        pack();
    }
    
}