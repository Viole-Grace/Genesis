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

### Keywords
- Extract( String , String , String , String ) is the caller function
- Extract( String user_text, String language, String notInTag, String inTag ) : user_text = text from which keywords need to be extracted, language = language of the given text, notInTag = PoS tag to be overlooked / negated, inTag = PoS tag to be searched for.
