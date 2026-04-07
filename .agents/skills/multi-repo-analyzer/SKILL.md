---
name: multi-repo-analyzer
description: "Analyze multiple code repositories in the workspace to understand their business logic, technical stack, and architecture. Generates detailed individual Markdown analysis documents and a comprehensive comparison report."
user-invocable: true
---

# Multi-Repository Analyzer & Comparator

## When to Use
- When the workspace contains multiple repositories or sub-projects solving similar problems.
- When you need a deep dive into the technical implementation, tech stack, and business logic of different codebases.
- When generating documentation or whitepapers to compare different technical approaches to determine pros and cons.

## Procedure

### Step 1: Repository Discovery & Context Gathering
1. Identify the target repositories or sub-folders in the workspace.
2. Read the key entry points (e.g., `main` functions, build files like `pom.xml`, `requirements.txt`, or `.sln` files).
3. Investigate the core business logic files for each repository, paying attention to the specific patterns used (e.g., AST parsing, OOP patterns, Regex matching).

### Step 2: Individual Repository Analysis
For each repository, systematically generate a precise Markdown report containing:
1. **Core Purpose**: Briefly explain what the tool/project does.
2. **Tech Stack**: List the programming language, core libraries, and runtime environment.
3. **Business Logic Flow**: Provide a start-to-finish execution logic analysis, detailing the technical implementation step-by-step.

*Action*: Save each report as `<RepoName>_Analysis.md` in the project root.

### Step 3: Comparative Evaluation
After all repositories are analyzed, evaluate them across several dimensions:
- Technical Architecture & Precision (e.g., script vs. compiler-level parsing).
- User Experience (UX) & Target Audience (CLI vs. GUI).
- Maintainability & Extensibility.

*Action*: Generate a final comparison report named `Repositories_Comparison_Report.md` in the project root, highlighting the pros, cons, and providing a conclusion matrix.

## Output Criteria
- All outputs must be well-formatted Markdown files placed systematically in the designated directory.
- Explanations must be highly technical and grounded in the actual codebase (avoiding vague summaries).
- Use tables for comparative matrices in the final report.