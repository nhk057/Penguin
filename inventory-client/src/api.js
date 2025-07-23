const BASE_URL = "http://localhost:4567"; 

export const getInventory = () => fetch(`${BASE_URL}/inventory`).then(res => res.json());
export const getOutOfStock = () => fetch(`${BASE_URL}/inventory/outOfStockItems`).then(res => res.json());
export const getOverStocked = () => fetch(`${BASE_URL}/inventory/overStocked`).then(res => res.json());
export const getUnderStocked = () => fetch(`${BASE_URL}/inventory/underStocked`).then(res => res.json());
export const getItemById = id => fetch(`${BASE_URL}/inventory/item/${id}`).then(res => res.json());
export const getCheapest = id => fetch(`${BASE_URL}/inventory/item/${id}/cheapest`).then(res => res.json());

export const getDistributors = () => fetch(`${BASE_URL}/distributors`).then(res => res.json());
export const getDistributorItems = id => fetch(`${BASE_URL}/distributors/${id}/items`).then(res => res.json());
export const getItemDistributors = id => fetch(`${BASE_URL}/item/${id}/distributors`).then(res => res.json());

export const addItem = data =>
  fetch(`${BASE_URL}/items`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(res => res.json());

export const updateInventory = data =>
  fetch(`${BASE_URL}/inventory/update`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(res => res.json());

export const addDistributor = data =>
  fetch(`${BASE_URL}/distributor/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(res => res.json());

export const addDistributorItem = data =>
  fetch(`${BASE_URL}/distributor/item`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(res => res.json());

export const deleteItem = id =>
  fetch(`${BASE_URL}/item/delete/${id}`, { method: "DELETE" }).then(res => res.json());

export const deleteDistributor = id =>
  fetch(`${BASE_URL}/distributor/delete/${id}`, { method: "DELETE" }).then(res => res.json());
