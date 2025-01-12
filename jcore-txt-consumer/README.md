# JCore Text Consumer
A simple text consumer.

**Descriptor Path**:
```
de.julielab.jcore.consumer.txt.desc.jcore-txt-consumer
```
### Objective
The JULIE Lab SentenceTokenAnnotator is a UIMA Consumer (CC). It has two output modes TOKEN and DOCUMENT.
In TOKEN mode, it outputs sentences (one per line), tokens (white-space-separated) and appends POS information to each token, if avaiable and desired, separated with a designated delimiter (e.g. $ or | or _).
In DOCUMENT mode, it just outputs the CAS document text string without any changes. For this mode, no annotations are required.

### Requirements and Dependencies
The input of a CC is done via annotation objects. The classes corresponding to these objects are part of the [JCoRe Type System](https://github.com/JULIELab/jcore-base/tree/master/jcore-types). The output should be a valid txt-file.

### Using the CC - Descriptor Configuration
In UIMA, each component is configured by a descriptor in XML. Such a preconfigured descriptor is available under `src/main/resources/de/julielab/jcore/consumer/txt/desc` but it can be further edited if so desired; see [UIMA SDK User's Guide](https://uima.apache.org/downloads/releaseDocs/2.1.0-incubating/docs/html/tools/tools.html#ugr.tools.cde) for further information.

**1. Parameters**

| Parameter Name | Parameter Type | Mandatory | Multivalued | Description |
|----------------|----------------|-----------|-------------|-------------|
| outDirectory | String | yes | yes | Path to an output directory |
| delimiter | String | no | no | If the delimiter is stated, the consumer will append the POS information to each token |
| mode | String | no | no | The output mode, TOKEN or DOCUMENT. Defaults to TOKEN. Determines whether to output sentences and tokens (TOKEN) or just the document text (DOCUMENT) |
| gzip | Boolean | no | no | Whether or not to store the output files in GZIP format. |

**2. Predefined Settings**

| Parameter Name | Parameter Syntax | Example |
|----------------|------------------|---------|
| outDirectory | Valid Path to an output directory | `data` |
| delimiter | A designated/chosen delimiter | `$` |
| mode | Either TOKEN or DOCUMENT. | TOKEN |
| gzip | true or false | true |

**3. Capabilities**

| Type | Input | Output |
|------|:-----:|:------:|
| de.julielab.jcore.types.Header | `+` |  |
| de.julielab.jcore.types.Sentence | `+` |  |
| de.julielab.jcore.types.POSTag | `+` |  |
| de.julielab.jcore.types.Token | `+` |  |
