import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:8080"
});

export const getDashboardStats = (username, password) =>
    API.get("/dashboard/stats", {
        auth: {
            username,
            password
        }
    });

export const getUserActivity = (username, password) =>
    API.get("/dashboard/users", {
        auth: {
            username,
            password
        }
    });

export const getSuspiciousActivity = (username, password) =>
    API.get("/dashboard/suspicious", {
        auth: {
            username,
            password
        }
    });

export default API;