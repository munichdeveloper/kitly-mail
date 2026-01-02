# Example API Calls for Kitly Mail Service

## Prerequisites
- Service running on localhost:8080
- Basic auth credentials configured (default: admin/admin)

## Send Email

```bash
curl -X POST http://localhost:8080/api/emails \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "fromEmail": "sender@example.com",
    "fromName": "John Doe",
    "toEmail": "recipient@example.com",
    "toName": "Jane Smith",
    "subject": "Test Email from Kitly Mail",
    "htmlContent": "<h1>Hello!</h1><p>This is a test email.</p>",
    "textContent": "Hello! This is a test email."
  }'
```

## Get Email by ID

```bash
curl -X GET http://localhost:8080/api/emails/1 \
  -u admin:admin
```

## Get All Emails

```bash
curl -X GET http://localhost:8080/api/emails \
  -u admin:admin
```

## Filter by Status

```bash
# Get all sent emails
curl -X GET http://localhost:8080/api/emails?status=SENT \
  -u admin:admin

# Get all failed emails
curl -X GET http://localhost:8080/api/emails?status=FAILED \
  -u admin:admin
```

## Filter by Recipient

```bash
curl -X GET "http://localhost:8080/api/emails?recipient=user@example.com" \
  -u admin:admin
```

## Using Environment Variables

```bash
# Set credentials
export API_USER=admin
export API_PASS=admin
export API_URL=http://localhost:8080

# Send email
curl -X POST $API_URL/api/emails \
  -u $API_USER:$API_PASS \
  -H "Content-Type: application/json" \
  -d @email.json
```

## Sample email.json

```json
{
  "fromEmail": "noreply@example.com",
  "fromName": "Kitly Service",
  "toEmail": "user@example.com",
  "toName": "User Name",
  "subject": "Welcome to Kitly",
  "htmlContent": "<html><body><h1>Welcome!</h1><p>Thanks for joining Kitly.</p></body></html>",
  "textContent": "Welcome! Thanks for joining Kitly."
}
```
