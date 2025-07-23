import React, { useState } from "react";
import {
  getInventory, getOutOfStock, getOverStocked, getUnderStocked,
  getItemById, getCheapest, getDistributors, getDistributorItems,
  getItemDistributors, addItem, updateInventory, addDistributor,
  addDistributorItem, deleteItem, deleteDistributor
} from "./api";

function App() {
  const [data, setData] = useState(null);
  const [input, setInput] = useState({});

  const handleChange = (e) => {
    setInput({ ...input, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (fn) => {
    const res = await fn(input);
    setData(res);
  };

  const handleGet = async (fn) => {
    const res = await fn();
    setData(res);
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>🧪 TopBloc Inventory API Tester</h2>

      {/* 🔍 GET requests */}
      <div>
        <button onClick={() => handleGet(getInventory)}>All Inventory</button>
        <button onClick={() => handleGet(getOutOfStock)}>Out of Stock</button>
        <button onClick={() => handleGet(getOverStocked)}>Overstocked</button>
        <button onClick={() => handleGet(getUnderStocked)}>Understocked</button>
        <button onClick={() => handleSubmit(() => getItemById(input.itemId))}>Item by ID</button>
        <button onClick={() => handleSubmit(() => getCheapest(input.itemId))}>Cheapest for Item ID</button>
        <button onClick={() => handleGet(getDistributors)}>All Distributors</button>
        <button onClick={() => handleSubmit(() => getDistributorItems(input.distributorId))}>Distributor Items</button>
        <button onClick={() => handleSubmit(() => getItemDistributors(input.itemId))}>Item Distributors</button>
      </div>

      {/* 📝 Inputs */}
      <div style={{ marginTop: 20 }}>
        <input name="itemId" placeholder="Item ID" onChange={handleChange} />
        <input name="distributorId" placeholder="Distributor ID" onChange={handleChange} />
      </div>

      {/* ➕ POST/PUT */}
      <div style={{ marginTop: 20 }}>
        <h4>Add Item</h4>
        <input name="id" placeholder="ID" onChange={handleChange} />
        <input name="name" placeholder="Name" onChange={handleChange} />
        <input name="stock" placeholder="Stock" onChange={handleChange} />
        <input name="capacity" placeholder="Capacity" onChange={handleChange} />
        <button onClick={() => handleSubmit(addItem)}>Add Item</button>
        <button onClick={() => handleSubmit(updateInventory)}>Update Inventory</button>
      </div>

      <div>
        <h4>Add Distributor</h4>
        <input name="id" placeholder="Distributor ID" onChange={handleChange} />
        <input name="name" placeholder="Distributor Name" onChange={handleChange} />
        <input name="item" placeholder="Item ID" onChange={handleChange} />
        <input name="cost" placeholder="Cost" onChange={handleChange} />
        <button onClick={() => handleSubmit(addDistributor)}>Add Distributor</button>
        <button onClick={() => handleSubmit(addDistributorItem)}>Add Item to Distributor</button>
      </div>

      {/* ❌ DELETE */}
      <div>
        <h4>Delete</h4>
        <button onClick={() => handleSubmit(() => deleteItem(input.itemId))}>Delete Item</button>
        <button onClick={() => handleSubmit(() => deleteDistributor(input.distributorId))}>Delete Distributor</button>
      </div>

      {/* 🖨️ Output */}
      <pre style={{ background: "#eee", padding: 10, marginTop: 20, maxHeight: 300, overflowY: "auto" }}>
        {JSON.stringify(data, null, 2)}
      </pre>
    </div>
  );
}

export default App;
