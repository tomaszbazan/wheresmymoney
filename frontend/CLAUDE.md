# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands
- Build: `flutter build <platform>` (apk, ios, web)
- Run: `flutter run` or `flutter run -d <device_id>`
- Lint: `flutter analyze`
- Format: `flutter format lib/`
- Test: `flutter test`
- Single test: `flutter test test/path_to_test.dart`
- Hot reload: `r` (while app is running)

## Architecture

This is a Flutter expense tracking app with a clean architecture pattern:

### Structure
- **lib/main.dart**: Entry point that launches MainScreen
- **lib/screens/**: Page-level widgets (MainScreen orchestrates tab navigation)
- **lib/widgets/**: Reusable UI components (SideMenu handles collapsible navigation)
- **lib/models/**: Data models (Account, Expense with JSON serialization)
- **lib/services/**: HTTP service layer for REST API communication

### Key Components
- **MainScreen**: Uses TabController + SideMenu for desktop-style navigation
- **SideMenu**: Animated collapsible sidebar with expand/collapse functionality
- **Services**: HTTP-based services expecting API at localhost:8080/api
- **Models**: Handle flexible JSON parsing (int/double conversion, optional fields)

## Code Style Guidelines
- Use const constructor when possible
- Prefer const over final when values are compile-time constants
- Use named parameters for constructors
- Organize imports alphabetically: dart:core first, then package:flutter, then project imports
- Follow Flutter style guide (camelCase for variables/methods, UpperCamelCase for classes)
- Prefix private members with underscore (_)
- Provide type annotations for public APIs
- Use === for widget rebuilding optimization
- Handle errors with try/catch and provide meaningful error messages
- Separate UI components into smaller widgets
- Use StatelessWidget when possible instead of StatefulWidget
- Don't use comments in code, except very complicated, unclear logic