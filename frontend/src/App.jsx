import { useEffect, useState } from "react";

import {
  getDashboardStats,
  getUserActivity,
  getSuspiciousActivity
} from "./services/api";

import "./App.css";
import Login from "./Login";


function App() {

  const [showLogin, setShowLogin] = useState(true);

  const [stats, setStats] = useState({
    totalAttempts: 0,
    successfulAttempts: 0,
    failedAttempts: 0,
    suspiciousActivities: 0
  });

  const [users, setUsers] = useState([]);
  const [suspicious, setSuspicious] = useState([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");


  // =========================
  // LOAD DASHBOARD
  // =========================

  const loadDashboard = async () => {

    try {

      setLoading(true);
      setError("");

      const [
        statsResponse,
        usersResponse,
        suspiciousResponse
      ] = await Promise.all([

        getDashboardStats(),

        getUserActivity(),

        getSuspiciousActivity()

      ]);

      setStats(statsResponse.data);

      setUsers(usersResponse.data);

      setSuspicious(suspiciousResponse.data);

    } catch (err) {

      console.error(err);

      if (err.response?.status === 401) {

        setError("Authentication required.");

      } else if (err.response?.status === 403) {

        setError(
            "You do not have permission to view the dashboard."
        );

      } else {

        setError(
            "Unable to load dashboard data."
        );
      }

    } finally {

      setLoading(false);
    }
  };


  // =========================
  // AFTER OTP SUCCESS
  // =========================

  const handleLoginSuccess = () => {

    setShowLogin(false);

    loadDashboard();
  };


  // =========================
  // LOGIN PAGE
  // =========================

  if (showLogin) {

    return (
        <Login
            onLoginSuccess={handleLoginSuccess}
        />
    );
  }


  // =========================
  // DASHBOARD LOADING
  // =========================

  if (loading) {

    return (
        <div className="loading">
          Loading dashboard...
        </div>
    );
  }


  // =========================
  // DASHBOARD
  // =========================

  return (

      <div className="dashboard">

        <header className="header">

          <div>

            <h1>
              Login Activity Monitor
            </h1>

            <p>
              Security monitoring dashboard
            </p>

          </div>


          <div>

            <button
                className="refresh-button"
                onClick={loadDashboard}
            >
              Refresh
            </button>

          </div>

        </header>


        {error && (

            <div className="error">
              {error}
            </div>

        )}


        {/* =========================
                STATISTICS
            ========================= */}

        <section className="stats-grid">

          <div className="stat-card">

                    <span>
                        Total Attempts
                    </span>

            <strong>
              {stats.totalAttempts}
            </strong>

          </div>


          <div className="stat-card">

                    <span>
                        Successful Logins
                    </span>

            <strong>
              {stats.successfulAttempts}
            </strong>

          </div>


          <div className="stat-card">

                    <span>
                        Failed Logins
                    </span>

            <strong>
              {stats.failedAttempts}
            </strong>

          </div>


          <div className="stat-card">

                    <span>
                        Suspicious Activities
                    </span>

            <strong>
              {stats.suspiciousActivities}
            </strong>

          </div>

        </section>


        {/* =========================
                USER ACTIVITY
            ========================= */}

        <section className="panel">

          <div className="panel-header">

            <h2>
              User Activity
            </h2>

            <span>
                        {users.length} users
                    </span>

          </div>


          <div className="table-container">

            <table>

              <thead>

              <tr>

                <th>
                  Username
                </th>

                <th>
                  Total Attempts
                </th>

                <th>
                  Successful
                </th>

                <th>
                  Failed
                </th>

              </tr>

              </thead>


              <tbody>

              {users.length === 0 ? (

                  <tr>

                    <td colSpan="4">
                      No user activity found.
                    </td>

                  </tr>

              ) : (

                  users.map((user) => (

                      <tr key={user.username}>

                        <td>
                          {user.username}
                        </td>

                        <td>
                          {user.totalAttempts}
                        </td>

                        <td className="success-text">
                          {user.successfulAttempts}
                        </td>

                        <td className="failure-text">
                          {user.failedAttempts}
                        </td>

                      </tr>

                  ))

              )}

              </tbody>

            </table>

          </div>

        </section>


        {/* =========================
                SUSPICIOUS ACTIVITY
            ========================= */}

        <section className="panel">

          <div className="panel-header">

            <h2>
              Suspicious Activity Timeline
            </h2>

            <span>
                        {suspicious.length} events
                    </span>

          </div>


          <div className="table-container">

            <table>

              <thead>

              <tr>

                <th>
                  Time
                </th>

                <th>
                  Username
                </th>

                <th>
                  IP Address
                </th>

                <th>
                  Reason
                </th>

              </tr>

              </thead>


              <tbody>

              {suspicious.length === 0 ? (

                  <tr>

                    <td colSpan="4">
                      No suspicious activity found.
                    </td>

                  </tr>

              ) : (

                  suspicious.map((activity) => (

                      <tr key={activity.id}>

                        <td>
                          {new Date(
                              activity.timestamp
                          ).toLocaleString()}
                        </td>

                        <td>
                          {activity.username ||
                              "Unknown"}
                        </td>

                        <td>
                          {activity.ipAddress}
                        </td>

                        <td className="warning-text">
                          {activity.reason}
                        </td>

                      </tr>

                  ))

              )}

              </tbody>

            </table>

          </div>

        </section>

      </div>
  );
}


export default App;