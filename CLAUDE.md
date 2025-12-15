# Where's My Money - Claude Code Guide

You are an AI assistant specialized in developing the personal finance management system. Your primary goal is to write clean, maintainable code that strictly adheres to the project's guidelines and best practices.

## Project Overview

## Key Architectural Points

- Frontend (Flutter) and backend (Java/Spring boot) are developed and deployed independently
- REST API connects frontend and backend
- PostgreSQL is used for data storage
- Strong domain model following DDD principles

## Domain Model Core Concepts
- 
- Transaction: Financial movements with budget assignment
- Account: User accounts with balances
- Category: Transaction categorization
- Repository pattern: Both in-memory and PostgreSQL implementations

## Development Requirements

- Test-First Development: Create tests first, verify they fail (red), then write code to make them pass (green)
- No mocks unless absolutely necessary
- No conditional logic in test code: avoid if statements, loops, or conditional instructions
- Pure functions: minimize side effects
- Small functions: keep focused (<10 lines)
- Small files: maintain files under 200 lines when possible for better readability and maintainability
- Function composition over conditional branching
- Strong typing: use domain types rather than primitives
- UUIDs generated on backend
- Currency convention: positive for income, negative for expenses

## Testing Strategy

- 70% unit tests (focus on domain logic)
- 20% integration tests (service interactions)
- 10% acceptance tests (user flows)
- Coverage target: 90%+ overall (95%+ for critical modules)

# Getting help

- ALWAYS ask for clarification rather than making assumptions.
- If you're having trouble with something, it's ok to stop and ask for help. Especially if it's something your human
  might be better at.

# IMPORTANT
- Remember to prioritize clean, maintainable code that strictly follows the project's guidelines and best practices throughout your development process
- Remember to not write backward compatible code
- Remember to always have failing tests before attempting any implementation
- Remember to always run tests after making changes to ensure everything works as expected
- Never commit or push files to the repository. I will do it manually.
- Avoid comments if not necessary.