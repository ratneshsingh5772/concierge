# 💰 Personal Finance Concierge

A friction-free way to track spending without opening a spreadsheet! This AI-powered assistant helps you log expenses, monitor budgets, and generate reports using natural language.

## 🌟 Features

- **Natural Language Processing**: Just say "I spent $15 on coffee" - the AI understands!
- **Smart Categorization**: Automatically categorizes expenses (Food, Transport, Entertainment)
- **Budget Monitoring**: Real-time budget tracking with warnings when you're close to limits
- **Monthly Reports**: Generate comprehensive spending reports instantly
- **Web UI**: Beautiful, modern interface for easy interaction
- **REST API**: Programmatic access for integrations
- **CLI Interface**: Terminal-based interaction option
- **Session Management**: Multiple users with isolated sessions

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Google Gemini API Key ([Get one here](https://aistudio.google.com/app/apikey))

### Installation

1. **Clone or navigate to the project:**
   ```bash
   cd /home/ratnesh/Documents/concierge
   ```

2. **Set up your API key:**
   ```bash
   export GOOGLE_API_KEY="your-gemini-api-key-here"
   ```
   
   Or create a `.env` file:
   ```bash
   echo 'export GOOGLE_API_KEY="your-key-here"' > .env
   source .env
   ```

3. **Build the project:**
   ```bash
   ./mvnw clean package
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Access the Web UI:**
   Open your browser to: http://localhost:8081

## 📱 Usage

### Web Interface

1. Open http://localhost:8081 in your browser
2. Type your expense in natural language
3. Use quick action buttons for common queries
4. View your budget status in the side panel

**Example queries:**
- "I spent $15 on coffee"
- "Paid $50 for groceries"
- "What's my Food budget status?"
- "Show me my monthly report"
- "I spent $25 on Uber"

### REST API

#### Log an Expense
```bash
curl -X POST http://localhost:8081/api/chat/message \
     -H "Content-Type: application/json" \
     -d '{"message": "I spent $15 on coffee", "userId": "user1"}'
```

#### Check Budget
```bash
curl -X POST http://localhost:8081/api/chat/message \
     -H "Content-Type: application/json" \
     -d '{"message": "What is my Food budget?", "userId": "user1"}'
```

#### Get Monthly Report
```bash
curl -X POST http://localhost:8081/api/chat/message \
     -H "Content-Type: application/json" \
     -d '{"message": "Show my monthly report", "userId": "user1"}'
```

#### Reset Session
```bash
curl -X POST http://localhost:8081/api/chat/reset \
     -H "Content-Type: application/json" \
     -d '{"userId": "user1"}'
```

#### Health Check
```bash
curl http://localhost:8081/api/chat/health
```

### CLI Interface

When you run the application, you can interact directly in the terminal:

```bash
./mvnw spring-boot:run
```

Then type commands:
```
> I spent $15 on coffee
> What's my Food budget?
> Show me my monthly report
> exit
```

## 🧪 Testing

### Automated Tests
```bash
./test-concierge.sh
```

### Manual Testing
See the comprehensive [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed testing instructions.

## 💡 How It Works

### Architecture

```
┌─────────────────┐
│   Web UI        │
│  (React-like)   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ChatController  │
│  (REST API)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  FinanceAgent   │
│  (Google ADK)   │
└────────┬────────┘
         │
    ┌────┴────┬────────┬──────────┐
    ▼         ▼        ▼          ▼
┌────────┐ ┌──────┐ ┌──────┐ ┌───────┐
│ Log    │ │Budget│ │Report│ │ CSV   │
│Expense │ │Status│ │      │ │Storage│
└────────┘ └──────┘ └──────┘ └───────┘
```

### Technologies

- **Spring Boot 3.x**: Modern Java framework
- **Google ADK**: AI agent development kit
- **Google Gemini API**: Large language model
- **OpenCSV**: CSV file management
- **Lombok**: Boilerplate reduction
- **Spring WebFlux**: Reactive streams for real-time responses
- **Vanilla JavaScript**: Clean, dependency-free frontend

### Budget Categories

| Category      | Monthly Limit |
|---------------|---------------|
| Food          | $200         |
| Transport     | $100         |
| Entertainment | $150         |

These can be modified in `FinanceAgent.java`.

## 📂 Project Structure

```
concierge/
├── src/
│   ├── main/
│   │   ├── java/com/finance/concierge/
│   │   │   ├── ConciergeApplication.java   # Main application
│   │   │   ├── FinanceAgent.java           # AI agent with tools
│   │   │   └── controller/
│   │   │       └── ChatController.java     # REST API
│   │   └── resources/
│   │       ├── application.properties      # Configuration
│   │       └── static/                     # Web UI files
│   │           ├── index.html
│   │           ├── css/style.css
│   │           └── js/app.js
│   └── test/
├── expenses.csv                            # Generated expense data
├── test-concierge.sh                       # Test script
├── TESTING_GUIDE.md                        # Testing documentation
├── README.md                               # This file
└── pom.xml                                 # Maven configuration
```

## 🔧 Configuration

### application.properties

```properties
# Server Port
server.port=8081

# Logging
logging.level.com.finance.concierge=INFO

# Actuator
management.endpoints.web.exposure.include=health,info
```

### Environment Variables

- `GOOGLE_API_KEY`: Your Gemini API key (required)

## 🐛 Troubleshooting

### Port 8081 Already in Use
```bash
kill -9 $(lsof -t -i:8081)
```

### API Key Not Set
```bash
export GOOGLE_API_KEY="your-key-here"
```

### Rate Limit (429 Error)
Wait 2-3 minutes between requests. Free tier limits:
- 15 requests per minute
- 1,500 requests per day

### Cannot Write to CSV
```bash
chmod 666 expenses.csv
```

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for more troubleshooting tips.

## 📊 Example Interactions

### Logging Expenses
```
You: I spent $15 on coffee this morning
AI: Logged $15.00 to Food

You: Paid $50 for an Uber ride
AI: Logged $50.00 to Transport
```

### Checking Budgets
```
You: What's my Food budget status?
AI: You have spent $15.00 out of $200.00 on Food. Remaining: $185.00.

You: Am I close to my Transport limit?
AI: You have spent $50.00 out of $100.00 on Transport. Remaining: $50.00.
```

### Monthly Reports
```
You: Show me my monthly spending report
AI: Monthly Spending Report:
- Food: $65.00
- Transport: $75.00
- Entertainment: $30.00
```

## 🔒 Security Notes

- API keys should be stored in environment variables, never in code
- The `.env` file is gitignored
- Session data is stored in-memory (resets on restart)
- CSV file permissions should be properly set

## 🚧 Limitations

- Currently supports 3 expense categories (expandable)
- Data stored in CSV (can be migrated to database)
- In-memory sessions (not persistent across restarts)
- Free tier API rate limits apply

## 🛣️ Roadmap

- [ ] Add more expense categories
- [ ] Database integration (PostgreSQL/MySQL)
- [ ] Persistent session storage
- [ ] User authentication
- [ ] Data visualization charts
- [ ] Export to PDF/Excel
- [ ] Mobile app
- [ ] Recurring expense tracking
- [ ] Budget alerts via email/SMS

## 📝 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Support

For issues and questions:
1. Check [TESTING_GUIDE.md](TESTING_GUIDE.md)
2. Review troubleshooting section
3. Check application logs
4. Verify API key and rate limits

## 🙏 Acknowledgments

- Built with [Google ADK](https://github.com/google/adk)
- Powered by [Google Gemini AI](https://ai.google.dev/)
- Uses [Spring Boot](https://spring.io/projects/spring-boot)
- CSV handling by [OpenCSV](http://opencsv.sourceforge.net/)

---

**Made with ❤️ for hassle-free expense tracking**

For detailed testing instructions, see [TESTING_GUIDE.md](TESTING_GUIDE.md)

