import { useState } from "react";
import ReCAPTCHA from "react-google-recaptcha";
import axios from "axios";

const API_URL = "http://localhost:8080";

const SITE_KEY =
    "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI";

function Login({ onLoginSuccess }) {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [otp, setOtp] = useState("");

    const [captchaToken, setCaptchaToken] = useState("");
    const [captchaRequired, setCaptchaRequired] = useState(false);

    const [otpRequired, setOtpRequired] = useState(false);

    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    // STEP 1 - Username + Password
    const handleLogin = async (event) => {

        event.preventDefault();

        setMessage("");
        setLoading(true);

        try {

            const response = await axios.post(
                `${API_URL}/auth/login`,
                {
                    username,
                    password,
                    captchaToken: captchaToken || null
                }
            );

            console.log("Login response:", response.data);

            // Password is correct.
            // Backend has sent OTP.
            setOtpRequired(true);
            setCaptchaRequired(false);
            setCaptchaToken("");

            setMessage(
                "OTP sent to your registered email. Enter the OTP below."
            );

        } catch (error) {

            console.error(error);

            const status = error.response?.status;
            const data = error.response?.data;

            // Backend currently returns CAPTCHA errors as plain strings
            const errorMessage =
                typeof data === "string"
                    ? data
                    : data?.message || data?.error || "";

            if (
                status === 429 &&
                errorMessage.includes("CAPTCHA")
            ) {

                setCaptchaRequired(true);

                setMessage(
                    "Multiple failed attempts detected. Please complete the CAPTCHA."
                );

            } else if (
                status === 403 &&
                errorMessage.includes("CAPTCHA")
            ) {

                setCaptchaRequired(true);
                setCaptchaToken("");

                setMessage(
                    "CAPTCHA verification failed. Please try again."
                );

            } else {

                setMessage(
                    errorMessage || "Login failed."
                );
            }
        }finally {

            setLoading(false);
        }
    };


    // STEP 2 - Verify OTP
    const handleVerifyOtp = async (event) => {

        event.preventDefault();

        setMessage("");
        setLoading(true);

        try {

            const response = await axios.post(
                `${API_URL}/auth/verify-otp`,
                {
                    username,
                    otp
                }
            );

            console.log("OTP verification response:", response.data);

            setMessage("OTP verified. Opening dashboard...");

            // Tell App.jsx that authentication is complete
            setTimeout(() => {
                onLoginSuccess();
            }, 500);

        } catch (error) {

            console.error(error);

            const data = error.response?.data;

            setMessage(
                data?.message ||
                data?.error ||
                "Invalid or expired OTP."
            );

        } finally {

            setLoading(false);
        }
    };


    return (
        <div className="login-container">

            <div className="login-card">

                <h1>Login Activity Monitor</h1>

                <p>Secure Login</p>


                {!otpRequired ? (

                    // =========================
                    // LOGIN FORM
                    // =========================

                    <form onSubmit={handleLogin}>

                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) =>
                                setUsername(e.target.value)
                            }
                            required
                        />

                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />


                        {captchaRequired && (

                            <div className="captcha-container">

                                <ReCAPTCHA
                                    sitekey={SITE_KEY}
                                    onChange={(token) =>
                                        setCaptchaToken(token || "")
                                    }
                                    onExpired={() =>
                                        setCaptchaToken("")
                                    }
                                />

                            </div>

                        )}


                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? "Logging in..." : "Login"}
                        </button>

                    </form>

                ) : (

                    // =========================
                    // OTP FORM
                    // =========================

                    <form onSubmit={handleVerifyOtp}>

                        <input
                            type="text"
                            placeholder="Enter OTP"
                            value={otp}
                            onChange={(e) =>
                                setOtp(e.target.value)
                            }
                            maxLength="6"
                            required
                        />

                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading
                                ? "Verifying..."
                                : "Verify OTP"}
                        </button>

                    </form>

                )}


                {message && (

                    <div className="login-message">
                        {message}
                    </div>

                )}

            </div>

        </div>
    );
}

export default Login;