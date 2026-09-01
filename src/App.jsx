import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { useSession } from './context/SessionContext';

import Register from './pages/Register';
import Consent from './pages/Consent';
import Home from './pages/Home';
import Checkin from './pages/Checkin';
import CheckinResult from './pages/CheckinResult';
import CheckinHistory from './pages/CheckinHistory';

function ProtectedRoute({ children }) {
    const { profile, consent } = useSession();
    const location = useLocation();

    // If no profile, they must register.
    if (!profile && location.pathname !== '/register') {
        return <Navigate to="/register" replace />;
    }

    // If profile exists but no consent, they must consent.
    // Exception: allowing them to be on the register page if they want.
    if (profile && (!consent || !consent.isProvided) && location.pathname !== '/consent' && location.pathname !== '/register') {
        return <Navigate to="/consent" replace />;
    }

    // If profile and consent exist, but they are trying to access register or consent -> Redirect to home
    if (profile && consent && consent.isProvided && (location.pathname === '/register' || location.pathname === '/consent')) {
        return <Navigate to="/home" replace />;
    }

    return children;
}

export default function App() {
    return (
        <Router>
            <div className="app-container font-body flex flex-col min-h-screen">
                <main className="flex-grow flex flex-col">
                    <Routes>
                        <Route path="/register" element={<ProtectedRoute><Register /></ProtectedRoute>} />
                        <Route path="/consent" element={<ProtectedRoute><Consent /></ProtectedRoute>} />
                        <Route path="/home" element={<ProtectedRoute><Home /></ProtectedRoute>} />
                        <Route path="/checkin" element={<ProtectedRoute><Checkin /></ProtectedRoute>} />
                        <Route path="/checkin-result" element={<ProtectedRoute><CheckinResult /></ProtectedRoute>} />
                        <Route path="/checkin-history" element={<ProtectedRoute><CheckinHistory /></ProtectedRoute>} />

                        {/* Catch-all route mapping to /home which will redirect conditionally based on ProtectedRoute */}
                        <Route path="*" element={<Navigate to="/home" />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}
