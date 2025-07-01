package ksmanualtrader.windows;

import lombok.Getter;
import org.kynesys.foundation.v1.utils.LanguageKit;
import org.kynesys.graphite.v1.GraphiteProgramLauncher;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriverManifest;
import org.kynesys.kstraderapi.v1.driver.KSExchangeDriver;
import org.kynesys.kstraderapi.v1.objects.Order;

import javax.swing.*;
import java.util.ArrayList;

public class OrderList extends JFrame {

    private int numOfOrdersPerPage = 10;
    private int maxPage;
    private int currentPage;

    private KSExchangeDriver driver;

    private JLabel serverRefreshLatency;
    private JLabel currentPageLabel;
    private JButton nextButton;
    private JButton toLastButton;
    private JButton previousButton;
    private JButton toFirstButton;

    private ArrayList<Order> orders = new ArrayList<>();
    private ArrayList<OrderRow> rows = new ArrayList<>();
    private JPanel tablePanel;

    public OrderList(ArrayList<Order> initialList, KSExchangeDriverManifest driver) {
        this.orders.clear();
        this.orders.addAll(initialList); // Safe copy
        this.driver = driver.getDriver(GraphiteProgramLauncher.getJournalingObject());

        maxPage = initialList.size() / numOfOrdersPerPage + (initialList.size() % numOfOrdersPerPage == 0 ? 0 : 1);
        currentPage = 0;

        serverRefreshLatency = new JLabel();
        currentPageLabel = new JLabel("1/" + maxPage);
        toLastButton = new JButton(LanguageKit.getValue("ORDER_LIST_TO_LAST"));
        nextButton = new JButton(">");
        toFirstButton = new JButton(LanguageKit.getValue("ORDER_LIST_TO_FIRST"));
        previousButton = new JButton("<");

        rowFactory(0, numOfOrdersPerPage);
        refresh();
    }

    private void rowFactory(int startInclusive, int endExclusive) {
        for (int i = startInclusive; i < endExclusive; i++) {
            OrderRow row = new OrderRow(driver, orders.get(i), (int) (getWidth() * 0.9), (int) (getHeight() * 0.8 / numOfOrdersPerPage));
            rows.add(row);
        }
    }

    private void refresh() {
        // Refresh current page table based on number of page

        // Reconstruct table as well

        // Trigger by every button click

        // Update page buttons as well
    }

}

@Getter
class OrderRow extends JPanel {
    private KSExchangeDriver driver;
    private Order order;
    private JButton closeButton = new JButton(LanguageKit.getValue("ORDER_POSITION_CLOSE"));
    private JLabel amount = new JLabel();
    private JLabel evaluatedAmount = new JLabel();
    private JLabel entryPrice = new JLabel();
    private JLabel profitAndLoss = new JLabel();

    public OrderRow(KSExchangeDriver driver, Order order, int rowWidth, int rowHeight) {
        this.order = order;
        this.driver = driver;
    }
}
