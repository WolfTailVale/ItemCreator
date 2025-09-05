# Contributing to ItemCreator

Thank you for your interest in contributing to ItemCreator! We welcome contributions of all kinds.

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a new branch for your feature or bugfix
4. Make your changes
5. Test your changes
6. Submit a pull request

## Development Setup

### Prerequisites

- Java 21
- Git

### Building

```bash
./gradlew clean build
```

### Testing

- Build the plugin with `./gradlew build`
- Copy `build/libs/ItemCreator-0.1.0-dev.jar` to a Paper 1.21.1+ test server
- Test the `/createrecipe` and `/giveitem` commands

## Code Style

- Follow existing Java conventions in the codebase
- Use 4 spaces for indentation
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs

## Pull Request Guidelines

1. **Small, focused changes** - Keep PRs focused on a single feature or bugfix
2. **Clear descriptions** - Explain what your change does and why
3. **Test your changes** - Ensure the plugin builds and works correctly
4. **Update documentation** - Update README.md if you add new features

## Feature Ideas

We welcome contributions in these areas:

- **Recipe Types**: Support for smithing, furnace, brewing recipes
- **Item Behaviors**: Right-click actions, custom item interactions
- **Configuration**: Additional item properties and metadata options
- **Performance**: Optimizations for large servers
- **Documentation**: Examples, tutorials, API documentation

## Bug Reports

If you find a bug, please create an issue with:

- Steps to reproduce
- Expected behavior
- Actual behavior
- Server version and plugin version
- Any relevant console errors

## Questions?

Feel free to open an issue for questions about the codebase or development setup.

Thank you for contributing! 🎮
