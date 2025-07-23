package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static spark.Spark.*;

public class InventoryController {

    public void setupRoutes() {

        // get all inventory with item name, id, stock, and capacity
        get("/inventory", (req, res) -> {
            res.type("application/json");
            try {
                JSONArray result = DatabaseManager.getInventory();
                if (result == null) {
                    res.status(500);
                    return "{\"error\": \"Failed to retrieve inventory.\"}";
                }
                res.status(200);
                return result.toJSONString();
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // get all items that are out of stock (stock = 0)
        get("/inventory/outOfStockItems", (req, res) -> {
            res.type("application/json");
            try {
                JSONArray result = DatabaseManager.getOutOfStockItems();
                if (result == null) {
                    res.status(500);
                    return "{\"error\": \"Failed to retrieve out-of-stock items.\"}";
                }
                res.status(200);
                return result.toJSONString();
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // get all items that are overstocked (stock > capacity)
        get("/inventory/overStocked", (req, res) -> {
            res.type("application/json");
            try {
                JSONArray result = DatabaseManager.getOverStockedItems();
                if (result == null) {
                    res.status(500);
                    return "{\"error\": \"Failed to retrieve overstocked items.\"}";
                }
                res.status(200);
                return result.toJSONString();
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // get all items that are understocked (stock < 35% of capacity)
        get("/inventory/underStocked", (req, res) -> {
            res.type("application/json");
            try {
                JSONArray result = DatabaseManager.getUnderStockedItems();
                if (result == null) {
                    res.status(500);
                    return "{\"error\": \"Failed to retrieve understocked items.\"}";
                }
                res.status(200);
                return result.toJSONString();
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // get a single item by id and return name, stock, capacity
        get("/inventory/item/:id", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                JSONArray result = DatabaseManager.displayItem(id);

                if (result == null || result.isEmpty()) {
                    res.status(404);
                    return String.format("{\"error\": \"Item with ID %d not found.\"}", id);
                }

                res.status(200);
                return result.toJSONString();
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\": \"Invalid item ID format.\"}";
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // get the cheapest distributor for an item by id
        get("/inventory/item/:id/cheapest", (req, res) -> {
            res.type("application/json");
            try {
                int itemId = Integer.parseInt(req.params(":id"));
                JSONObject result = DatabaseManager.getCheapestQuantity(itemId);

                if (result == null) {
                    res.status(404);
                    return String.format("{\"error\": \"No distributors found for item ID %d.\"}", itemId);
                }

                res.status(200);
                return result.toJSONString();

            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\": \"Invalid item ID format.\"}";
            } catch (Exception e) {
                res.status(500);
                return String.format("{\"error\": \"Unexpected server error: %s\"}", e.getMessage());
            }
        });

        // update inventory for an item by id (stock and capacity)
        put("/inventory/update", (req, res) -> {
            try {
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());

                int id = Integer.parseInt(body.get("id").toString());
                int stock = Integer.parseInt(body.get("stock").toString());
                int capacity = Integer.parseInt(body.get("capacity").toString());

                String result = DatabaseManager.updateInventory(id, stock, capacity);

                JSONObject response = new JSONObject();
                res.type("application/json");

                if (result.equals("success")) {
                    res.status(200);
                    response.put("message", "Inventory updated successfully.");
                } else if (result.equals("item not found")) {
                    res.status(404);
                    response.put("error", "Item with ID " + id + " not found.");
                } else {
                    res.status(500);
                    response.put("error", "Inventory update failed.");
                }

                return response.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid input: " + e.getMessage());
                return error.toJSONString();
            }
        });
    }
}
