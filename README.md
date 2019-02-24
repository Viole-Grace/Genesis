## Genesis

- A developmental custom package based on NLP for small scale deployment.


### Pre-requisites

- apache-opennlp
- coreNLP
- Java SE8 (LTS) [minimum]

check dependancies using opennlp CLI
```markdown
$ >opennlp
```
Note that Java libraries are not system-wide, and hence classpaths need to lead to the same directory as where the package is located.

# Package files :

### Summarizer
- Summarize( String , int ) is the caller function
- Summarize(String text, int maxSummarySize) : text = textual data that needs summarization, maxSummarySize = length of summary in sentences.
