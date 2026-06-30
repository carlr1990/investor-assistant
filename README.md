# Investor Assistant
## Architecture

The Investor Assistant is a Spring Boot application that uses Spring AI to provide personalized portfolio insights to investors. It combines deterministic calculations (for financial metrics) with generative AI (for natural language responses).

### Technology Stack

- **Backend**: Spring Boot 3.2.0 with Java 17
- **AI**: Spring AI with OpenAI GPT-4
- **Database**: H2 (in-memory) for prototype
- **Data Processing**: OpenCSV for data loading
- **Build Tool**: Maven

### Why Spring AI?

Spring AI provides a clean abstraction over LLM providers, allowing us to:
1. Swap models easily (GPT-4, Claude, etc.)
2. Use a consistent API for prompt engineering
3. Integrate seamlessly with Spring ecosystem
4. Handle context management and token optimization

## How It Works

1. **Data Loading**: All CSV files are loaded into H2 database on startup
2. **Portfolio Calculations**: Business logic computes metrics (MOIC, current value, etc.)
3. **Personalization**: Investor profile (age, tech savviness) determines response style
4. **AI Integration**: System prompt includes portfolio data + personalization parameters
5. **Response Generation**: GPT-4 generates grounded, personalized responses

## Running the Application

### Prerequisites
- Java 21
- Maven 3.6+

### Steps

1. Clone the repository
2. Build and Run - Command line into the project path and run following commands:
	mvn clean install
	mvn spring-boot:run

   Alternatively import project into an IDE Run a Maven build and Run as a Spring Application.
3. Access API in command line or in an API platform (eg. Postman) at 
	
	http://localhost:8080/api/assistant/chat

	Example Usage :

	curl -X POST http://localhost:8080/api/assistant/chat -H "Content-Type: application/json" -d '{ "investorId": "INV001", "question": "What is my current portfolio value and MOIC?" }'
			
	or

	curl -X POST http://localhost:8080/api/assistant/chat -H "Content-Type: application/json" -d "{\"investorId\": \"INV001\", \"question\": \"What is my current portfolio value and MOIC?\"}"



### Assumptions

Data is complete and accurate as of the report date

Investor authentication is handled externally

All monetary values are converted to investor's reporting currency

Report date is June 25, 2026


### Known Limitations

Prototype Scale: In-memory H2 database won't scale to production

No RAG: Current implementation uses all data in prompt (works for 112 investors, 550 allocations)

No Real-time Data: Valuations are static as of report date

No Multi-turn Context: Each question is independent

Rate Limiting: No handling of API rate limits


### Future Improvements

Implement RAG with vector embeddings for scale

Add persistent conversation memory

Implement caching for portfolio calculations

Add multi-modal support for chart generation

Implement hybrid search with deterministic + semantic matching

Add proper logging and monitoring

Implement testing for response styles




