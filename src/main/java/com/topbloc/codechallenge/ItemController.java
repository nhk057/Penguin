package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static spark.Spark.*;

public class ItemController {
    
    public void setupRoutes() {

        // get all items from the items table
        get("/items", (req, res) -> {
            res.type("application/json");
            return DatabaseManager.getItems().toJSONString();
        });

        // get all distributors for a specific item using the item id
        get("/item/:id/distributors", (req, res) -> {
            res.type("application/json");

            try {
                int itemId = Integer.parseInt(req.params(":id"));
                JSONArray result = DatabaseManager.displayItemDistributors(itemId);

                if (result == null) {
                    res.status(500);
                    JSONObject error = new JSONObject();
                    error.put("error", "Failed to retrieve distributor data for item ID " + itemId);
                    return error.toJSONString();
                }

                res.status(200);
                return result.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid item ID.");
                return error.toJSONString();
            }
        });

        // add a new item to items and inventory table
        post("/items", (req, res) -> {
            res.type("application/json");

            try {
                JSONObject json = (JSONObject) new JSONParser().parse(req.body());

                int id = ((Long) json.get("id")).intValue();
                String name = json.get("name").toString();
                int stock = ((Long) json.get("stock")).intValue();
                int capacity = ((Long) json.get("capacity")).intValue();

                String result = DatabaseManager.addItem(id, name, stock, capacity);

                if ("success".equals(result)) {
                    res.status(201);
                    return "{\"message\":\"Item added successfully\"}";
                } else if ("duplicates".equals(result)) {
                    res.status(409);
                    return "{\"error\":\"Item already exists\"}";
                } else {
                    res.status(500);
                    return "{\"error\":\"Database error\"}";
                }

            } catch (Exception e) {
                res.status(400);
                return "{\"error\":\"Invalid request\"}";
            }
        });

        // delete an item from the items table using its id
        delete("/item/delete/:id", (req, res) -> {
            res.type("application/json");

            try {
                int itemId = Integer.parseInt(req.params(":id"));
                String result = DatabaseManager.deleteItem(itemId);

                JSONObject response = new JSONObject();
                if (result.equals("Deletion successful")) {
                    res.status(200);
                    response.put("message", result);
                } else if (result.equals("Item not found.")) {
                    res.status(404);
                    response.put("error", result);
                } else {
                    res.status(500);
                    response.put("error", result);
                }

                return response.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid ID format.");
                return error.toJSONString();
            }
        });
    }
}
