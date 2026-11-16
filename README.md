
# Continuum

## Introduction

Continuum is an Android application designed to improve shift-to-shift communication and reduce information loss during employee handoffs.

In workplaces that rely on multiple shifts, important information can easily be missed, forgotten, or buried in informal notes and conversations. Continuum provides a centralized system where employees can document shift activity, track unresolved issues, assign ownership, and review information from previous shifts.

The goal of Continuum is to make shift handoffs more organized, searchable, and reliable while reducing the amount of time employees spend trying to determine what happened during an earlier shift.

## Features

Continuum includes the following features:

- **User Authentication** – Allows users to securely sign in to the application.
- **Shift Handoff Management** – Allows employees to create, edit, view, and acknowledge shift handoffs.
- **AI-Assisted Note Structuring** – Helps organize unstructured shift information into clear and useful handoff records.
- **Quick Notes** – Allows users to create, edit, save, delete, and convert notes into handoffs.
- **Unresolved Issue Tracking** – Keeps ongoing problems and tasks visible between shifts.
- **Issue Ownership** – Identifies who is responsible for unresolved items.
- **Incoming-Shift Summaries** – Provides incoming employees with an overview of important information from previous shifts.
- **Searchable History** – Allows users to review previous handoff records.
- **Search and Filtering** – Helps users locate relevant handoffs and issues.
- **Comments and Action History** – Allows users to document updates, attempted actions, and progress on handoff items.
- **Attachments** – Supports attaching relevant files and media to handoff records.
- **Voice Recording and Transcription** – Allows users to record audio, transcribe speech, and play back recordings.
- **Priority Notifications** – Highlights high-priority handoffs and provides notification indicators.
- **Supervisor Dashboard** – Provides an overview of handoff activity and outstanding issues.
- **Trends and Analytics** – Displays information about handoff activity and trends.
- **Data Encryption** – Uses AES-256-GCM encryption to protect sensitive information in newly created and updated handoffs.
- **User Profiles and Settings** – Allows users to access profile information and manage application preferences.

## Technologies

Continuum was developed using the following technologies:

- **Kotlin** – Primary programming language
- **Android Studio** – Primary development environment
- **Jetpack Compose** – Android user interface development
- **Gradle** – Build and dependency management
- **Supabase** – Backend services, authentication, database access, and file storage
- **PostgreSQL** – Relational database used through Supabase
- **Supabase Edge Functions** – Server-side processing for handoff operations
- **AES-256-GCM** – Encryption for sensitive handoff information
- **Android Speech Recognition** – Speech-to-text functionality
- **Git** – Version control
- **GitHub** – Repository hosting and team collaboration
- **Figma** – UI/UX wireframing and prototyping
- **Jira** – Sprint planning and project management

## Security

Continuum includes security measures designed to protect user information and sensitive shift records.

User authentication is managed through Supabase Authentication.

Sensitive handoff fields, including titles and content, are encrypted using AES-256-GCM through Supabase Edge Functions before being stored in the database. The application also supports decrypting this information so users can continue viewing and managing their handoffs normally.

Encryption was implemented later in the development process, so handoffs created before this feature was introduced remain stored as plaintext. New handoffs and subsequent updates use the encryption process.

Database access is also managed through Supabase security policies.

These measures help protect newly created and updated handoff information while maintaining the application's functionality.

## Installation

Continuum was developed as part of the Full Sail University Computer Science Capstone program and is not currently available as a public production release.

Developers can clone the repository and build the Android application using Android Studio.

### 1. Clone the Repository

Open a terminal or Git Bash and run:

```bash
git clone https://github.com/TehMajesticSnek/Continuum--FS-Capstone.git
```

### 2. Navigate to the Android Project

```bash
cd Continuum--FS-Capstone/Continuum
```

### 3. Open the Project

Open Android Studio, select **Open**, and navigate to the Continuum directory inside the cloned repository.

### 4. Sync the Project

Allow Android Studio to complete Gradle synchronization and download the required dependencies.

### 5. Configure Backend Services

Continuum uses Supabase for authentication, database operations, storage, and Edge Functions.

A compatible Supabase configuration and the required backend services must be available for connected functionality to work correctly.

Private credentials and encryption keys should not be committed to the repository.

### 6. Select a Device

Create or select an Android Virtual Device using Android Studio's Device Manager.

A compatible physical Android device with Developer Mode and USB Debugging enabled may also be used.

### 7. Run the Application

Select the desired device and click the **Run** button in Android Studio to compile and launch Continuum.

## Development Setup

Developers contributing to Continuum should have the following installed:

- Git
- Android Studio
- Android SDK
- A compatible Java/JDK installation
- Android emulator or compatible physical Android device
- Access to the required Supabase backend configuration

### Clone the Project

```bash
git clone https://github.com/TehMajesticSnek/Continuum--FS-Capstone.git
cd Continuum--FS-Capstone/Continuum
```

Open the Continuum directory in Android Studio.

Android Studio should recognize the Gradle project and begin synchronizing the required dependencies.

Allow Gradle synchronization to finish before attempting to compile or run the application.

## Branching

Developers should create feature branches for new functionality instead of making changes directly to the main branch.

For example:

```bash
git checkout -b feature/example-feature
```

After a feature has been completed and tested, changes can be reviewed before being merged into the appropriate development branch.

## Building the Application

To create a development build:

1. Open the Continuum project in Android Studio.
2. Allow Gradle synchronization to complete.
3. Select an Android emulator or connected physical device.
4. Select **Run > Run 'app'**.
5. Verify that the application successfully compiles and launches.

Developers can also use **Build > Make Project** to verify that the project compiles successfully without launching the application.

## Repository Structure

The Continuum repository separates the Android application from other Capstone project resources.

```text
Continuum--FS-Capstone/
├── Assets/
├── Builds/
├── Continuum/
├── Documentation/
├── Shared/
└── README.md
```

### Important Paths

- **Continuum/** – Main Android Studio application project
- **Assets/** – Project assets and related resources
- **Builds/** – Application builds and build-related files
- **Documentation/** – Project documentation
- **Shared/** – Shared project resources

## Development Process

Continuum was developed collaboratively using an Agile/Scrum approach.

Jira was used to organize development tasks, track progress, and manage feature implementation.

Git and GitHub were used for version control and collaboration throughout the project.

Development involved implementing application features, integrating backend services, troubleshooting issues, testing functionality, and improving the user interface.

## Testing

Testing was performed throughout development to verify that application features worked correctly.

Testing included:

- User authentication and application navigation
- Handoff creation, editing, and retrieval
- Handoff acknowledgements
- Search and filtering
- Quick Notes creation, editing, deletion, and conversion
- Attachments and multimedia functionality
- Voice transcription and playback
- Priority notifications
- Encryption and decryption of handoff information
- Supervisor Dashboard functionality
- Trends and Analytics functionality

Issues identified during testing were addressed through debugging and additional development.

## Project Milestones

The following major milestones were completed during development:

- Established the application concept and design
- Created the Android application interface
- Integrated Supabase backend services
- Implemented user authentication
- Developed shift handoff management
- Implemented Quick Notes functionality
- Added unresolved issue tracking and ownership
- Integrated AI-assisted handoff organization
- Added multimedia functionality
- Implemented priority notifications
- Developed supervisor and analytics features
- Implemented AES-256-GCM encryption for sensitive handoff information
- Conducted application testing and debugging
- Prepared the application for the final Capstone presentation

## Known Limitations

Continuum was developed as an educational Capstone project and has not been deployed as a publicly supported production application.

Handoff records created before encryption was implemented remain stored as plaintext. A migration process would be needed to encrypt those historical records.

The application also requires its configured Supabase backend services to operate correctly.

Production deployment, broader compatibility testing, and long-term maintenance are outside the scope of the completed Capstone development.

## License

This project was created for educational purposes as part of the Full Sail University Computer Science Capstone program.

Licensing terms have not yet been established for this project.

## Contributors

### Team 3 – Continuum

**Lucas McDonough – Technical Lead / Developer**

Responsibilities include:

- Technical leadership and development coordination
- Application architecture and system design
- Android application development
- Backend development and Supabase integration
- PostgreSQL database design and management
- User authentication and backend connectivity
- Database queries and data retrieval
- Search and filtering functionality
- Integration of frontend and backend components
- Handoff data management and storage
- Troubleshooting and debugging
- Application testing and performance improvements
- GitHub collaboration and code integration

**Dominic Taylor – Developer / Product & UI**

Responsibilities include:

- Initial application concept and product vision
- Original design document and UI/UX style guide
- Android application development
- Frontend and UI implementation
- Shift handoff acknowledgements and incoming-shift summaries
- Comments and action history
- Priority notifications
- Quick Notes development and interface improvements
- Multimedia integration, voice transcription, and playback
- AES-256-GCM encryption implementation
- Application testing, troubleshooting, and documentation

Continuum was developed collaboratively by both members of Team 3. Each contributed to the application's development, integration, testing, and overall completion.

## Project Status

**Capstone Development Complete – Final Presentation Preparation**

Continuum's planned core functionality has been implemented, and the application has reached the final stage of the Full Sail University Computer Science Capstone project.

The team has completed the primary development work and is preparing the application and supporting documentation for the final presentation.

Continuum remains an educational project and is not currently offered as a public production release.

## Repository

The Continuum source code and project files are available on GitHub:

https://github.com/TehMajesticSnek/Continuum--FS-Capstone
