# Continuum

## Introduction

**Continuum** is an Android application designed to improve shift-to-shift communication and reduce information loss during employee handoffs.

In workplaces that rely on multiple shifts, important information can easily be missed, forgotten, or buried in informal notes and conversations. Continuum provides a centralized system where employees can document shift activity, track unresolved issues, assign ownership, and review information from previous shifts.

The goal of Continuum is to make shift handoffs more organized, searchable, and reliable while reducing the amount of time employees spend trying to determine what happened during an earlier shift.

## Features

Continuum is being developed with the following core features:

* **User Authentication** – Allows users to securely sign in to the application.
* **Shift Handoff Records** – Provides a structured location for employees to document important information from their shifts.
* **AI-Assisted Note Structuring** – Helps organize shift notes into clear and useful handoff information.
* **Unresolved Issue Tracking** – Keeps ongoing problems and tasks visible between shifts.
* **Issue Ownership** – Identifies who is responsible for unresolved items.
* **Next-Shift Summaries** – Provides incoming employees with an overview of important information from the previous shift.
* **Searchable History** – Allows users to review previous shift records.
* **Filtering** – Helps users locate relevant records and issues.
* **User Profiles and Notifications** – Provides access to user information and application notifications.
* **Settings** – Allows users to manage application preferences.

Some features are still under development and may not yet be fully functional.

## Technologies

Continuum is being developed as an Android application using the following technologies:

* **Kotlin** – Primary programming language
* **Android Studio** – Primary development environment
* **Jetpack Compose** – Android user interface development
* **Gradle** – Build and dependency management
* **Git** – Version control
* **GitHub** – Repository hosting and team collaboration
* **Figma** – UI/UX wireframing and prototyping
* **Jira** – Sprint planning and project management

Additional database technologies, APIs, and AI services will be documented as they are implemented.

## Installation

> **Note:** Continuum is currently in active development and is not yet available as a production release.

Users or developers who want to run the current development version can clone the GitHub repository and build the application using Android Studio.

### 1. Clone the Repository

Open a terminal or Git Bash and run:

```bash id="ppvov8"
git clone https://github.com/TehMajesticSnek/Continuum--FS-Capstone.git
```

### 2. Navigate to the Android Project

```bash id="b31frv"
cd Continuum--FS-Capstone/Continuum
```

### 3. Open the Project

Open **Android Studio**, select **Open**, and navigate to the `Continuum` directory inside the cloned repository.

### 4. Sync the Project

Allow Android Studio to complete the Gradle synchronization process and download any required dependencies.

### 5. Select a Device

Create or select an Android Virtual Device using Android Studio's **Device Manager**.

A physical Android device with Developer Mode and USB Debugging enabled may also be used.

### 6. Run the Application

Select the desired device and click the **Run** button in Android Studio to compile and launch Continuum.

## Development Setup

New developers contributing to Continuum should have the following installed:

* Git
* Android Studio
* Android SDK
* A compatible Java/JDK installation
* Android emulator or compatible physical Android device

### Clone the Project

```bash id="bn9gu7"
git clone https://github.com/TehMajesticSnek/Continuum--FS-Capstone.git
cd Continuum--FS-Capstone/Continuum
```

Open the `Continuum` directory in Android Studio.

Android Studio should recognize the Gradle project and begin synchronizing the required dependencies.

Allow Gradle synchronization to finish before attempting to compile or run the application.

### Branching

Developers should create feature branches for new functionality instead of making changes directly to the main branch.

For example:

```bash id="4vbv1b"
git checkout -b feature/example-feature
```

After a feature has been completed and tested, changes can be reviewed before being merged into the appropriate development branch.

### Building the Application

To create an initial development build:

1. Open the `Continuum` project in Android Studio.
2. Allow Gradle synchronization to complete.
3. Select an Android emulator or connected physical device.
4. Select **Run > Run 'app'**.
5. Verify that the application successfully compiles and launches.

Developers can also use **Build > Make Project** to verify that the project compiles successfully without launching the application.

## Repository Structure

The Continuum repository separates the Android application from other Capstone project resources.

```text id="a7m8qi"
Continuum--FS-Capstone/
├── Assets/
├── Builds/
├── Continuum/
├── Documentation/
├── Shared/
└── README.md
```

### Important Paths

* **`Continuum/`** – Main Android Studio application project
* **`Assets/`** – Project assets and related resources
* **`Builds/`** – Application builds and build-related files
* **`Documentation/`** – Project documentation
* **`Shared/`** – Shared project resources

The repository structure may continue to change as development progresses.

## Known Issues

Continuum is currently under active development. Because the project is in the Alpha stage, some functionality may be incomplete or subject to change.

Current development work includes completing application screens, connecting frontend functionality to backend services, implementing data storage, and integrating planned application features.

Additional bugs and development tasks are identified and addressed throughout each development sprint.

## Roadmap

Planned development for Continuum includes:

* Complete user authentication
* Complete application navigation and primary user interface
* Implement shift handoff creation and storage
* Implement searchable shift history
* Add record filtering
* Implement unresolved issue tracking
* Implement issue ownership
* Integrate AI-assisted shift note organization
* Generate next-shift summaries
* Complete user profile functionality
* Complete notifications and settings
* Connect frontend components to backend functionality
* Perform application testing and bug fixes
* Prepare a stable Capstone release

## License

This project was created for educational purposes as part of the Full Sail University Computer Science Capstone program.

Licensing terms have not yet been established for this project.

## Contributors

### Team 3 – Continuum

**Lucas McDonough – Technical Lead**

Responsibilities include:

* Technical direction and implementation
* Backend development
* Database functionality
* Search and filtering functionality
* Application architecture
* Integration of application components
* Project testing and troubleshooting

**Dominic Taylor – Developer / Product & UI**

Responsibilities include:

* Product vision and workflow
* Android application development
* Frontend and UI implementation
* AI-assisted note structuring
* Feature planning
* Project testing and documentation

Continuum is developed and maintained collaboratively by the members of Team 3.


## Project Status

**Alpha – Active Development**

Continuum is currently being developed as a Full Sail University Computer Science Capstone project.

Core functionality, user interfaces, data storage, and application features are still being implemented and tested. The application should currently be considered an **Alpha build** and is not intended for production use.

The project will continue to receive updates throughout the Capstone development process.

## Repository

The Continuum source code and project files are available on GitHub:

https://github.com/TehMajesticSnek/Continuum--FS-Capstone
