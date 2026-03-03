# Mediscan API Reference

Base URL: `/api`

## Authentication
| Method | Endpoint | Description | Payload |
|--------|----------|-------------|---------|
| POST | `/auth/register` | Register a new user | `{username, email, password, role}` |
| POST | `/auth/login` | Login and get JWT | `{email, password}` |

## Users
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users/me` | Get current user profile | Yes |

## Medicines
| Method | Endpoint | Description | Payload | Auth Required |
|--------|----------|-------------|---------|---------------|
| GET | `/medicines` | List all medicines | - | Yes |
| POST | `/medicines` | Add new medicine | `{name, description, barcode}` | Yes |
| GET | `/medicines/{id}` | Get medicine details | - | Yes |

## Inventory
| Method | Endpoint | Description | Payload | Auth Required |
|--------|----------|-------------|---------|---------------|
| GET | `/inventories` | List inventory | - | Yes |
| POST | `/inventories` | Set inventory stock | `{medicineId, quantity, threshold}` | Yes |
| POST | `/inventories/{id}/adjust` | Adjust stock (+/-) | `?amount=5` | Yes |

## Reminders
| Method | Endpoint | Description | Payload | Auth Required |
|--------|----------|-------------|---------|---------------|
| GET | `/reminders` | List active reminders | - | Yes |
| POST | `/reminders` | Create reminder | `{medicineId, reminderTime}` | Yes |
| POST | `/reminders/{id}/taken` | Mark as taken | - | Yes |
