# Future Roadmap - Planned Features

**Status**: All phases PLANNED (not yet started)

These phases are optional and can be pursued after the core Java implementation is stable.

[Back to Summary](BACKLOG_SUMMARY.md)

---

## Phase 4: Backend Services (120 Story Points)

**Goal**: Create a REST API backend for web-based plot generation.

### Epic 4.1: Spring Boot Application
- Spring Boot 3.2 project setup with security
- User authentication (JWT, RBAC)
- Plot CRUD API
- Data file upload API
- Plot rendering API (PNG, SVG, PDF)
- WebSocket for real-time updates
- Template management API

### Epic 4.2: API Documentation
- OpenAPI/Swagger documentation
- Comprehensive integration test suite

**Technologies**: Spring Boot 3.2, PostgreSQL, Redis, JWT, Spring Security

---

## Phase 5: Web Frontend (250 Story Points)

**Goal**: Create a modern web application for interactive plot creation.

### Epic 5.1: Frontend Setup
- React + TypeScript + Vite
- Material-UI components
- Zustand state management
- React Query for API calls

### Epic 5.2: Core Pages
- Landing page with examples
- Authentication (login, register, password reset)
- Dashboard with plot list

### Epic 5.3: Plot Creation Interface
- Drag-and-drop data upload
- Visual plot type selector
- Data column mapping interface
- Plot configuration panel
- Live preview with Plotly.js
- Monaco code editor for advanced users

### Epic 5.4: Export and Sharing
- Export to PNG, SVG, PDF
- Shareable links with privacy controls
- Embed code generation

### Epic 5.5: Polish
- Responsive mobile design
- Keyboard shortcuts
- Onboarding tutorial
- Loading states and feedback

**Technologies**: React 18, TypeScript, Vite, Plotly.js, Three.js, Material-UI

---

## Phase 6: Integration & Production (115 Story Points)

**Goal**: Integrate all components and prepare for production deployment.

### Epic 6.1: End-to-End Integration
- Full user journey E2E testing (Playwright/Cypress)
- Performance testing and optimization
- Security audit and hardening (OWASP)

### Epic 6.2: Documentation and Deployment
- User documentation and video tutorials
- Developer API documentation
- Kubernetes deployment
- CI/CD pipeline to production
- Monitoring (Prometheus/Grafana)
- Backup strategy
- Beta launch program

**Technologies**: Kubernetes, Prometheus, Grafana, Playwright

---

## Summary

| Phase | Story Points | Description |
|-------|-------------|-------------|
| Phase 4 | 120 | Backend REST API |
| Phase 5 | 250 | Web Frontend |
| Phase 6 | 115 | Integration & Production |
| **Total** | **485** | Full web application |

---

## Prerequisites

Before starting these phases:
- Phase 3 (Rendering) should be substantially complete
- Core demo pass rate should be >80%
- Unit test coverage should remain >70%

---

## Notes

- These phases are **optional** for CLI-only usage
- Backend (Phase 4) enables web-based plot generation
- Frontend (Phase 5) requires Backend to be complete
- Phase 6 is for production-ready deployment

**Last Updated**: 2025-12-16
