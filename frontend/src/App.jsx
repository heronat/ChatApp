import './App.css'

import { ThemeProvider } from "@material-tailwind/react";
import ProtectedRoute from "./utils/ProtectedRoute.jsx";
import {BrowserRouter as Router, Navigate, Route, Routes} from 'react-router-dom';
import LoginPage from "./authentication/Login.jsx";
import ChatsPage from "./pages/chats.pages.jsx";
import NotFound from "./pages/NotFound.page.jsx";
import PasswordResetRequest from "./authentication/PasswordResetRequest.jsx";
import RegisterPage from "./authentication/Register.jsx";
import ManageChatPage from "./pages/manage.chat.page.jsx";
import InviteChatPage from "./pages/invite.chat.page.jsx";
import InvitationManagePage from "./pages/invitation.manage.page.jsx";
import ChatPages from "./pages/chat.pages.jsx";
import PasswordReset from "./authentication/PasswordReset.jsx";


function App() {

  return (
      <ThemeProvider>
          <Router>
              <Routes>
                  <Route element={<ProtectedRoute />}>
                      <Route path="/" element={<ChatsPage />} />
                      <Route path="/chat" element={<ChatPages />} />
                      <Route path="/create-chat" element={<ManageChatPage />} />
                      <Route path="/edit-chat" element={<ManageChatPage />} />
                      <Route path="/invite" element={<InviteChatPage />} />
                      <Route path="/invitations" element={<InvitationManagePage />} />
                  </Route>
                  <Route path="/login" element={<LoginPage />} />
                  <Route path="/register" element={<RegisterPage />} />
                  <Route path="/password-reset-request" element={<PasswordResetRequest />} />
                  <Route path="/password-reset" element={<PasswordReset />} />
                  <Route path="/not-found" element={<NotFound />} />
                  <Route path="*" element={<Navigate to="/not-found" />} />
              </Routes>
          </Router>
      </ThemeProvider>
  )
}

export default App
