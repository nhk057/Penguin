package com.topbloc.codechallenge.db;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatabaseManager {
    private static final String jdbcPrefix = "jdbc:sqlite:";
    private static final String dbName = "challenge.db";
    private static String connectionString;
    private static Connection conn;

    static {
        File dbFile = new File(dbName);
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
    }

    public static void connect() {
        try {
            Connection connection = DriverManager.getConnection(connectionString);
            System.out.println("Connection to SQLite has been established.");
            conn = connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    public static void resetDatabase() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        File dbFile = new File(dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
        connectionString = jdbcPrefix + dbFile.getAbsolutePath();
        connect();
        applySchema();
        seedDatabase();
    }

    // Schema function to reset the database if needed - do not change
    private static void applySchema() {
        String itemsSql = "CREATE TABLE IF NOT EXISTS items (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String inventorySql = "CREATE TABLE IF NOT EXISTS inventory (\n"
                + "id integer PRIMARY KEY,\n"
                + "item integer NOT NULL UNIQUE references items(id) ON DELETE CASCADE,\n"
                + "stock integer NOT NULL,\n"
                + "capacity integer NOT NULL\n"
                + ");";
        String distributorSql = "CREATE TABLE IF NOT EXISTS distributors (\n"
                + "id integer PRIMARY KEY,\n"
                + "name text NOT NULL UNIQUE\n"
                + ");";
        String distributorPricesSql = "CREATE TABLE IF NOT EXISTS distributor_prices (\n"
                + "id integer PRIMARY KEY,\n"
                + "distributor integer NOT NULL references distributors(id) ON DELETE CASCADE,\n"
                + "item integer NOT NULL references items(id) ON DELETE CASCADE,\n"
                + "cost float NOT NULL\n" +
                ");";

        try {
            System.out.println("Applying schema");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Schema applied");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Schema function to reset the database if needed - do not change
    private static void seedDatabase() {
        String itemsSql = "INSERT INTO items (id, name) VALUES (1, 'Licorice'), (2, 'Good & Plenty'),\n"
                + "(3, 'Smarties'), (4, 'Tootsie Rolls'), (5, 'Necco Wafers'), (6, 'Wax Cola Bottles'), (7, 'Circus Peanuts'), (8, 'Candy Corn'),\n"
                + "(9, 'Twix'), (10, 'Snickers'), (11, 'M&Ms'), (12, 'Skittles'), (13, 'Starburst'), (14, 'Butterfinger'), (15, 'Peach Rings'), (16, 'Gummy Bears'), (17, 'Sour Patch Kids'),\n"
                + "(18, 'Hi-Chew'), (19, 'Gummy Sharks'), (20, 'Crunch Bars')";

        String inventorySql = "INSERT INTO inventory (item, stock, capacity) VALUES\n"
                + "(1, 22, 25), (2, 4, 20), (3, 15, 25), (4, 30, 50), (5, 14, 15), (6, 8, 10), (7, 10, 10), (8, 30, 40), (9, 17, 70), (10, 43, 65),\n"
                + "(11, 32, 55), (12, 25, 45), (13, 8, 45), (14, 10, 60), (15, 20, 30), (16, 15, 35), (17, 14, 60),\n"
                + "(18, 0, 25), (19, 0, 20), (20, 0, 30)";

        String distributorSql = "INSERT INTO distributors (id, name) VALUES (1, 'Candy Corp'), (2, 'The Sweet Suite'), (3, 'Dentists Hate Us'), (4, 'Yummy Factory')";

        String distributorPricesSql = "INSERT INTO distributor_prices (distributor, item, cost) VALUES \n"
                + "(1, 1, 0.81), (1, 2, 0.46), (1, 3, 0.89), (1, 4, 0.45),\n"
                + "(2, 2, 0.18), (2, 3, 0.54), (2, 4, 0.67), (2, 5, 0.25), (2, 6, 0.35), (2, 7, 0.23), (2, 8, 0.41), (2, 9, 0.54),\n"
                + "(2, 10, 0.25), (2, 11, 0.52), (2, 12, 0.07), (2, 13, 0.77), (2, 14, 0.93), (2, 15, 0.11), (2, 16, 0.42),\n"
                + "(3, 10, 0.47), (3, 11, 0.84), (3, 12, 0.15), (3, 13, 0.07), (3, 14, 0.97), (3, 15, 0.39), (3, 16, 0.91), (3, 17, 0.85),\n"
                + "(4, 18, 0.65), (4, 19, 0.49), (4, 20, 0.73)";

        try {
            System.out.println("Seeding database");
            conn.createStatement().execute(itemsSql);
            conn.createStatement().execute(inventorySql);
            conn.createStatement().execute(distributorSql);
            conn.createStatement().execute(distributorPricesSql);
            System.out.println("Database seeded");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper methods to convert ResultSet to JSON - change if desired, but should
    // not be required
    private static JSONArray convertResultSetToJson(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<String> colNames = IntStream.range(0, columns)
                .mapToObj(i -> {
                    try {
                        return md.getColumnName(i + 1);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            jsonArray.add(convertRowToJson(rs, colNames));
        }
        return jsonArray;
    }

    private static JSONObject convertRowToJson(ResultSet rs, List<String> colNames) throws SQLException {
        JSONObject obj = new JSONObject();
        for (String colName : colNames) {
            obj.put(colName, rs.getObject(colName));
        }
        return obj;
    }

    // Controller functions - add your routes here. getItems is provided as an
    // example

    public static JSONArray getItems() {
        String sql = "SELECT * FROM items";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to retrieve all items from the database.
     * Each item includes its ID, name, stock, and capacity.
     *
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray getInventory() {
        String sql = "SELECT inventory.item AS id, items.name, inventory.stock, inventory.capacity " + "FROM inventory "
                + "JOIN items ON inventory.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to retrieve all items that are out of stocked in the
     * inventory table, stock = 0
     * Each item includes its ID, name, stock, and capacity.
     *
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray getOutOfStockItems() {
        String sql = "SELECT inventory.item AS id, items.name, inventory.stock, inventory.capacity " + "FROM inventory "
                + "JOIN items ON inventory.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("name", "id", "stock", "capacity");
            JSONArray outOfStockArray = new JSONArray();

            // Iterate through the results of the SQL query to find which rows have 0 in
            // stock
            while (set.next()) {
                int stock = set.getInt("stock");
                if (stock == 0) {
                    outOfStockArray.add(convertRowToJson(set, columnNames));
                }
            }
            return outOfStockArray;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to retrieve all items that are over stocked in the
     * inventory table, stock > capacity
     * Each item includes its ID, name, stock, and capacity.
     *
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray getOverStockedItems() {
        String sql = "SELECT inventory.item AS id, items.name, inventory.stock, inventory.capacity " + "FROM inventory "
                + "JOIN items ON inventory.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("name", "id", "stock", "capacity");
            JSONArray overStockedArray = new JSONArray();

            // Iterate through the results of the SQL query to find which rows have a great
            // stock than capacity
            while (set.next()) {
                int stock = set.getInt("stock");
                int capacity = set.getInt("capacity");
                if (stock > capacity) {
                    overStockedArray.add(convertRowToJson(set, columnNames));
                }
            }
            return overStockedArray;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to retrieve all items that are under stocked in the
     * inventory table, stock < 35% of capacity
     * Each item includes its ID, name, stock, and capacity.
     *
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray getUnderStockedItems() {
        String sql = "SELECT inventory.item AS id, items.name, inventory.stock, inventory.capacity " + "FROM inventory "
                + "JOIN items ON inventory.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("name", "id", "stock", "capacity");
            JSONArray underStockedArray = new JSONArray();

            // Iterate through the results of the SQL query to find which rows have a great
            // stock than capacity
            while (set.next()) {
                int stock = set.getInt("stock");
                int capacity = set.getInt("capacity");
                if (stock < (capacity * 0.35)) {
                    underStockedArray.add(convertRowToJson(set, columnNames));
                }
            }
            return underStockedArray;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to a single item with a dynamic route, display just the
     * item specified
     * Item includes ID, name, stock, and capacity.
     *
     * @params in int is passed to keep track of the id and store into a JSN array
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray displayItem(int match) {
        String sql = "SELECT inventory.item AS id, items.name, inventory.stock, inventory.capacity " + "FROM inventory "
                + "JOIN items ON inventory.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("name", "id", "stock", "capacity");
            JSONArray item = new JSONArray();

            // Iterate through the results of the SQL query to find which rows matches the
            // target specified
            while (set.next()) {
                int id = set.getInt("id");
                if (match == id) {
                    item.add(convertRowToJson(set, columnNames));
                    break;
                }
            }

            return item;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to display all distributors from distributor table
     * Distributors include name and id
     *
     * 
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray getDistributors() {
        String sql = "SELECT * FROM distributors";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            return convertResultSetToJson(set);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a GET request to display distributor and specific items they sell
     * Distributors include cost, item name, and itemID
     *
     * @params an int is passed to keep track of the id and store into a JSON array
     * @return A JSON array of all items, or null if an error occurs.
     */
    public static JSONArray displayDistributor(int match) {
        String sql = "SELECT distributor_prices.distributor, items.id, items.name, distributor_prices.cost "
                + "FROM distributor_prices " + "JOIN items ON distributor_prices.item = items.id";
        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("id", "name", "cost");
            JSONArray items = new JSONArray();

            // Iterate through the results of the SQL query to find which rows matches the
            // target specified
            while (set.next()) {
                int id = set.getInt("distributor");
                if (id == match) {
                    items.add(convertRowToJson(set, columnNames));
                }
            }

            return items;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

    }

    /**
     * Handles a GET request to a single item ID and returns all distributor
     * offerings for that item.
     * Each offering includes distributor ID, name, and cost to purchase that item.
     *
     * @param itemId the item ID to query for distributor offerings
     * @return A JSON array of all distributor offerings, or null if an error
     *         occurs.
     */
    public static JSONArray displayItemDistributors(int itemId) {
        String sql = "SELECT distributors.id, distributors.name, distributor_prices.cost " +
                "FROM distributor_prices " +
                "JOIN distributors ON distributor_prices.distributor = distributors.id " +
                "WHERE distributor_prices.item = " + itemId;

        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            List<String> columnNames = List.of("id", "name", "cost");
            JSONArray result = new JSONArray();

            // Iterate through the results of the SQL query to find which rows matches the
            // target specified
            while (set.next()) {
                result.add(convertRowToJson(set, columnNames));
            }

            return result;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    // Post/Put/Delete Methods

    /**
     * Handles a POST request to add a new item and its inventory information into
     * the database.
     * Also ensures item ID or name does not already exist in the items table.
     *
     * @param id       the unique item ID
     * @param name     the item name
     * @param stock    initial stock amount
     * @param capacity storage capacity for the item
     * @return A string response of "success", "duplicates", or "error" on failure.
     */
    public static String addItem(int id, String name, int stock, int capacity) {
        String sql1 = "SELECT COUNT(*) FROM items WHERE id = ? OR name = ?";
        String sql2 = "INSERT INTO items (id, name) VALUES (?, ?)";
        String sql3 = "INSERT INTO inventory (item, stock, capacity) VALUES (?, ?, ?)";

        try (
                PreparedStatement condition = conn.prepareStatement(sql1);
                PreparedStatement item = conn.prepareStatement(sql2);
                PreparedStatement inventory = conn.prepareStatement(sql3);) {
            condition.setInt(1, id);
            condition.setString(2, name);
            ResultSet set = condition.executeQuery();
            set.next();
            int count = set.getInt(1);

            // Checks for duplicates
            if (count > 0) {
                return "duplicates";
            }

            item.setInt(1, id);
            item.setString(2, name);
            item.executeUpdate();

            inventory.setInt(1, id);
            inventory.setInt(2, stock);
            inventory.setInt(3, capacity);
            inventory.executeUpdate();

            return "success";

        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

    }

    /**
     * Handles a PUT request to update the inventory table for an existing item.
     * Updates the stock and capacity values for the specified item ID.
     *
     * @param id       the item ID to update
     * @param stock    new stock value
     * @param capacity new capacity value
     * @return A string response of "success", "item not found", or "error" on
     *         failure.
     */
    public static String updateInventory(int id, int stock, int capacity) {
        String sql = "UPDATE inventory SET stock = ?, capacity = ? WHERE item = ?";

        try (PreparedStatement inventory = conn.prepareStatement(sql)) {
            inventory.setInt(1, stock);
            inventory.setInt(2, capacity);
            inventory.setInt(3, id);
            int updated = inventory.executeUpdate();

            return updated > 0 ? "success" : "item not found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Handles a POST request to add a new distributor to the distributors table.
     * Checks for duplicate name or ID before inserting.
     *
     * @param id     the unique distributor ID
     * @param name   the distributor name
     * @param itemID
     * @param cost
     * @return A string response of "success", "duplicate distributor", or "error"
     *         on failure.
     */
    public static String addDistributor(int id, String name, int itemID, float cost) {
        String sql1 = "SELECT COUNT(*) FROM distributors WHERE id = ? OR name = ?";
        String sql2 = "INSERT INTO distributors (id, name) VALUES (?, ?)";

        try (
                PreparedStatement condition = conn.prepareStatement(sql1);
                PreparedStatement distributor = conn.prepareStatement(sql2);) {
            condition.setInt(1, id);
            condition.setString(2, name);
            ResultSet set = condition.executeQuery();
            set.next();
            int count = set.getInt(1);

            // Checks for duplicates
            if (count > 0) {
                return "duplicate distributor";
            }

            distributor.setInt(1, id);
            distributor.setString(2, name);
            distributor.executeUpdate();

            return "success";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Handles a POST request to add a new item to a distributor's catalog with
     * associated cost.
     * Ensures no duplicate entry exists for that item-distributor pair.
     *
     * @param distributorID the distributor ID
     * @param itemID        the item ID
     * @param cost          the cost at which this distributor offers the item
     * @return A string response of "success", "duplicate item", or "error" on
     *         failure.
     */
    public static String addDistributorItem(int distributorID, int itemID, float cost) {
        String sql1 = "SELECT COUNT(*) FROM distributor_prices WHERE distributor = ? AND item = ?";
        String sql2 = "INSERT INTO distributor_prices (distributor, item, cost) VALUES (?, ?, ?)";

        try (
                PreparedStatement condition = conn.prepareStatement(sql1);
                PreparedStatement prices = conn.prepareStatement(sql2);) {
            condition.setInt(1, distributorID);
            condition.setInt(2, itemID);
            ResultSet set = condition.executeQuery();
            set.next();
            int count = set.getInt(1);

            if (count > 0) {
                return "duplicate item";
            }

            prices.setInt(1, distributorID);
            prices.setInt(2, itemID);
            prices.setFloat(3, cost);
            prices.executeUpdate();

            return "success";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Handles a POST request to add a updated an item to a distributor's catalog with
     * associated cost.
     * Ensures no duplicate entry exists for that item-distributor pair.
     *
     * @param distributorID the distributor ID
     * @param itemID        the item ID
     * @param cost          the cost at which this distributor offers the item
     * @return A string response of "success", "duplicate item", or "error" on
     *         failure.
     */
    public static String updateDistributorItemPrice(int distributorID, int itemID, float newCost) {
        String sql = "UPDATE distributor_prices SET cost = ? WHERE distributor = ? AND item = ?";

        try (PreparedStatement update = conn.prepareStatement(sql)) {
            update.setFloat(1, newCost);
            update.setInt(2, distributorID);
            update.setInt(3, itemID);
            int updatedRows = update.executeUpdate();

            return updatedRows > 0 ? "success" : "item not found";
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    /**
     * Handles a GET request to retrieve the distributor offering the cheapest total
     * price
     * to restock a specific item based on its current stock and capacity.
     *
     * @param itemId the item ID to restock
     * @return A JSON object containing the best distributor, cost per unit, units
     *         needed,
     *         and total cost, or null if no matching entry is found or on error.
     */
    public static JSONObject getCheapestQuantity(int itemId) {
        String sql = "SELECT distributors.id, distributors.name, distributor_prices.cost, " +
                "inventory.stock, inventory.capacity " +
                "FROM distributor_prices " +
                "JOIN distributors ON distributor_prices.distributor = distributors.id " +
                "JOIN inventory ON distributor_prices.item = inventory.item " +
                "WHERE distributor_prices.item = " + itemId;

        try {
            ResultSet set = conn.createStatement().executeQuery(sql);
            JSONObject cheapestDistributor = new JSONObject();

            float minCost = 0;
            boolean found = false;

            while (set.next()) {
                found = true;
                int stock = set.getInt("stock");
                int capacity = set.getInt("capacity");
                float cost = set.getFloat("cost");

                int unitsNeeded = capacity - stock;
                float totalCost = cost * unitsNeeded;

                if (minCost == 0 || totalCost < minCost) {
                    minCost = totalCost;
                    cheapestDistributor = new JSONObject();
                    cheapestDistributor.put("id", set.getInt("id"));
                    cheapestDistributor.put("name", set.getString("name"));
                    cheapestDistributor.put("cost", cost);
                    cheapestDistributor.put("unitsNeeded", unitsNeeded);
                    cheapestDistributor.put("totalCost", totalCost);
                }
            }

            return found ? cheapestDistributor : null;

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Handles a DELETE request to remove an item from the items table.
     * Also removes the corresponding inventory entry due to ON DELETE CASCADE.
     *
     * @param itemId the item ID to delete
     * @return A string response of "Deletion successful", "Item not found.", or
     *         "Delete failed." on error.
     */
    public static String deleteItem(int itemId) {
        String sql = "DELETE FROM items WHERE id = ?";

        try (PreparedStatement item = conn.prepareStatement(sql)) {
            item.setInt(1, itemId);
            int deleted = item.executeUpdate();
            return deleted > 0 ? "Deletion successful" : "Item not found.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Delete failed.";
        }

    }

    /**
     * Handles a DELETE request to remove a distributor from the distributors table.
     * Also deletes any matching entries from the distributor_prices table due to ON
     * DELETE CASCADE.
     *
     * @param distributorId the distributor ID to delete
     * @return A string response of "Deletion successful", "Item not found.", or
     *         "Delete failed." on error.
     */
    public static String deleteDistributor(int distributorId) {
        String sql = "DELETE FROM distributors where id = ?";

        try (PreparedStatement item = conn.prepareStatement(sql)) {
            item.setInt(1, distributorId);
            int deleted = item.executeUpdate();
            return deleted > 0 ? "Deletion successful" : "Item not found.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Delete failed.";
        }
    }
}
