# Release guide

## Steps

1. Clone the `https://github.com/Sgitario/jester` repository from your local machine
2. Update the `.github/project.yml` with the new version:

```yml
name: Jester
release:
  current-version: 0.0.0
  next-version: 0.0.1-SNAPSHOT
```

3. Commit the changes and create a pull request (NOT from your fork)
4. Wait for the pull request to be green
5. Merge pull request
6. Wait for the release workflow is triggered: `https://github.com/Sgitario/jester/actions/workflows/release.yml`
7. Edit and publish the release draft: `https://github.com/Sgitario/jester/releases`