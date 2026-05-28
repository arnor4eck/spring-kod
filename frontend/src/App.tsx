import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import RegistrationPage from "./pages/RegistrationPage";
import AuthPage from "./pages/AuthPage";
import ProfilePage from "./pages/ProfilePage";
import MyDatasetsPage from "./pages/MyDatasetsPage";
import StarsPage from "./pages/StarsPage";
import CreateDatasetPage from "./pages/CreateDatasetPage";
import DatasetAnalyticsPage from "./pages/DatasetAnalyticsPage";
import DatasetEditPage from "./pages/DatasetEditPage";
import CollaboratorsPage from "./pages/CollaboratorsPage";
import ControversialLabelsPage from "./pages/ControversialLabelsPage";
import PoorQualityFilesPage from "./pages/PoorQualityFilesPage";
import DuplicatesPage from "./pages/DuplicatesPage";
import { useAuthStore } from "./store/authStore";

const PrivateRoute = ({ children }: { children: React.ReactNode }) => {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/register" element={<RegistrationPage />} />
        <Route path="/login" element={<AuthPage />} />

        <Route
          path="/profile"
          element={
            <PrivateRoute>
              <ProfilePage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets"
          element={
            <PrivateRoute>
              <MyDatasetsPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/create"
          element={
            <PrivateRoute>
              <CreateDatasetPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/analytics"
          element={
            <PrivateRoute>
              <DatasetAnalyticsPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/edit"
          element={
            <PrivateRoute>
              <DatasetEditPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/collaborators"
          element={
            <PrivateRoute>
              <CollaboratorsPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/controversial"
          element={
            <PrivateRoute>
              <ControversialLabelsPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/poor-quality"
          element={
            <PrivateRoute>
              <PoorQualityFilesPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/datasets/:id/duplicates"
          element={
            <PrivateRoute>
              <DuplicatesPage />
            </PrivateRoute>
          }
        />

        <Route
          path="/profile/stars"
          element={
            <PrivateRoute>
              <StarsPage />
            </PrivateRoute>
          }
        />

        <Route path="/" element={<Navigate to="/profile" />} />
      </Routes>
    </Router>
  );
}

export default App;
