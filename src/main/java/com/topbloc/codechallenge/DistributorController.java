package com.topbloc.codechallenge;

import com.topbloc.codechallenge.db.DatabaseManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import static spark.Spark.*;

public class DistributorController {

    public void setupRoutes() {

        // get all distributors with their id and name
        get("/distributors", (req, res) -> {
            res.type("application/json");
            try {
                return DatabaseManager.getDistributors().toJSONString();
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Failed to get distributors.\"}";
            }
        });

        // get all items sold by a distributor using their id
        get("/distributors/:id/items", (req, res) -> {
            res.type("application/json");
            try {
                int id = Integer.parseInt(req.params(":id"));
                return DatabaseManager.displayDistributor(id).toJSONString();
            } catch (NumberFormatException e) {
                res.status(400);
                return "{\"error\": \"Invalid distributor ID.\"}";
            } catch (Exception e) {
                res.status(500);
                return "{\"error\": \"Server error.\"}";
            }
        });

        get("/item/:id/distributors", (req, res) -> {
            int itemId = Integer.parseInt(req.params(":id"));
            JSONArray result = DatabaseManager.displayItemDistributors(itemId);

            if (result == null) {
                res.status(500);
                JSONObject error = new JSONObject();
                error.put("error", "Failed to retrieve distributor data for item ID " + itemId);
                return error.toJSONString();
            }

            res.status(200);
            res.type("application/json");
            return result.toJSONString();
        });

        // add a new distributor and item they sell
        post("/distributor/add", (req, res) -> {
            res.type("application/json");

            try {
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());

                int id = Integer.parseInt(body.get("id").toString());
                String name = body.get("name").toString();
                int item = Integer.parseInt(body.get("item").toString());
                float cost = Float.parseFloat(body.get("cost").toString());

                String result = DatabaseManager.addDistributor(id, name, item, cost);

                JSONObject response = new JSONObject();
                if (result.equals("success")) {
                    response.put("message", "Distributor added successfully.");
                    res.status(201);
                } else if (result.equals("duplicate distributor")) {
                    response.put("error", "Distributor already exists.");
                    res.status(409);
                } else {
                    response.put("error", "Internal error occurred.");
                    res.status(500);
                }

                return response.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid input format.");
                return error.toJSONString();
            }
        });

        // add item to distributor's catalog
        post("/distributor/item", (req, res) -> {
            res.type("application/json");

            try {
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());

                int distributorId = Integer.parseInt(body.get("distributor").toString());
                int itemId = Integer.parseInt(body.get("item").toString());
                float cost = Float.parseFloat(body.get("cost").toString());

                String result = DatabaseManager.addDistributorItem(distributorId, itemId, cost);

                JSONObject response = new JSONObject();
                switch (result) {
                    case "success":
                        res.status(201);
                        response.put("message", "Item added to distributor catalog.");
                        break;
                    case "duplicate item":
                        res.status(409);
                        response.put("error", "Item already exists for this distributor.");
                        break;
                    default:
                        res.status(500);
                        response.put("error", "Internal server error.");
                        break;
                }

                return response.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid input format.");
                return error.toJSONString();
            }
        });

        // update a distributor item
        post("/distributor/item/update", (req, res) -> {
            res.type("application/json");

            try {
                JSONObject body = (JSONObject) new JSONParser().parse(req.body());

                int distributorId = Integer.parseInt(body.get("distributor").toString());
                int itemId = Integer.parseInt(body.get("item").toString());
                float cost = Float.parseFloat(body.get("cost").toString());

                String result = DatabaseManager.updateDistributorItemPrice(distributorId, itemId, cost);

                JSONObject response = new JSONObject();
                switch (result) {
                    case "success":
                        res.status(200);
                        response.put("message", "Item price updated successfully.");
                        break;
                    case "item not found":
                        res.status(404);
                        response.put("error", "Item not found for distributor.");
                        break;
                    default:
                        res.status(500);
                        response.put("error", "Internal server error.");
                }

                return response.toJSONString();

            } catch (Exception e) {
                res.status(400);
                JSONObject error = new JSONObject();
                error.put("error", "Invalid input format.");
                return error.toJSONString();
            }
        });

        // delete a distributor by id
        delete("/distributor/delete/:id", (req, res) -> {
            res.type("application/json");
            JSONObject response = new JSONObject();

            try {
                int distributorId = Integer.parseInt(req.params(":id"));
                String result = DatabaseManager.deleteDistributor(distributorId);

                if (result.equals("Deletion successful")) {
                    response.put("message", result);
                    res.status(200);
                } else if (result.equals("Item not found.")) {
                    response.put("error", result);
                    res.status(404);
                } else {
                    response.put("error", result);
                    res.status(500);
                }

            } catch (Exception e) {
                res.status(400);
                response.put("error", "Invalid distributor ID.");
            }

            return response.toJSONString();
        });
    }
}
