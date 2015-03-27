irondetect
===============

This package contains the irondetect correlation engine. Based on contexts,
signatures and anomalies, it detects deviations from normal behavior.
It presents correlations between [IF-MAP][1] metadata of different devices.
By the definition of rules in a policy by a security expert, the correlated
results can be used to analyze a network.

If you need help for setting up, please contact the Trust@HsH team.

Development was done by [Hochschule Hannover][2] (Hannover University of Applied
Sciences and Arts) within the [ESUKOM][3] research project.

Documentation
=============

To be submitted later...

Build
=====
Just execute

	mvn package

in order to create a runnable jar file, a source jar file and this project
archive.

Feedback
========
If you have any questions, problems or comments, please contact

	trust@f4-i.fh-hannover.de

LICENSE
=======
Licensed under the [Apache License, Version 2.0][4].
You may not use this file except in compliance with the License.

[1]: http://www.trustedcomputinggroup.org/resources/tnc_ifmap_binding_for_soap_specification
[2]: http://trust.f4.hs-hannover.de
[3]: http://www.esukom.de
[4]: http://www.apache.org/licenses/LICENSE-2.0.html