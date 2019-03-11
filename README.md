# Logging-Level-Evolution-Plugin

[![Build Status](https://travis-ci.com/ponder-lab/Logging-Level-Evolution-Plugin.svg?token=gywSHb5G1W81zrovzorQ&branch=master)](https://travis-ci.com/ponder-lab/Logging-Level-Evolution-Plugin) [![Coverage Status](https://coveralls.io/repos/github/ponder-lab/Logging-Level-Evolution-Plugin/badge.svg?branch=master&t=SHx1bW)](https://coveralls.io/github/ponder-lab/Logging-Level-Evolution-Plugin?branch=master) [![GitHub license](https://img.shields.io/badge/license-Eclipse-blue.svg)](https://github.com/ponder-lab/Logging-Level-Evolution-Plugin/blob/master/LICENSE.txt) [![Java profiler](https://www.ej-technologies.com/images/product_banners/jprofiler_small.png)](https://www.ej-technologies.com/products/jprofiler/overview.html)

## Introduction

<img src="https://github.com/ponder-lab/Logging-Level-Evolution-Plugin/blob/master/edu.cuny.hunter.log.ui/icons/icon.png" alt="Icon" align="left"/> Developers usually choose log levels to filter information they would like to print. However, developers may not well estimate the cost and benefit for each log level, and log levels may change overtime with requirements. Our tools consist of Eclipse plugins which help developers adjust and rejuvenate log levels by using Degree of Interests (DOI) model. DOI value is a kind of real number for a program element which shows how developers are interested in it. It is computed from the interaction events between developer and element, such as developer edits the element. Transformation decision is made by analyzing the DOI values of enclosing methods for logging invocations.

## Screenshot

<img src="https://github.com/ponder-lab/Logging-Level-Evolution-Plugin/blob/master/edu.cuny.hunter.log.ui/icons/screenshot.png" alt="Screenshot" width=500px/>

## Usage

Our tool could be run in three different purposes. For each purpose, it could be run in two ways.

**Purposes**:

- Running transformation.
  - The command: "rejuvenate a log level".
- Running evaluator for log level rejuvenation.
  - The command: "evaluate log projects".
- Running evaluator for extracting method changes from git history.
  - The command: "evaluate mylyngit projects".

**Two ways**:

- Select projects -> Right click -> Choose "Refactor" -> Choose the command by different purpose you run the tool.

- Quick Assess -> Choose command.

**Notice**:
Make sure there must be an **active Mylyn task** before running tool.

## Options

We have four categories of option settings:

- Whether treat some log levels as a category not traditional log levels?
- Whether use git history?
- Whether lower log levels of logging statements in catch blocks.
- Limit the maximum number of git commits.

## Installation

### Update Site

Our tool can be installed via Eclipse Update Site at [https://raw.githubusercontent.com/ponder-lab/Logging-Level-Evolution-Plugin/master/edu.cuny.hunter.log.updatesite](https://raw.githubusercontent.com/ponder-lab/Logging-Level-Evolution-Plugin/master/edu.cuny.hunter.log.updatesite)

## Development platform

It is developed on Eclipse for RCP and RAP, and its version is 2018-09. The download page: https://www.eclipse.org/downloads/packages/release/kepler/sr2/eclipse-rcp-and-rap-developers

## Dependencies

Tycho: right click on the compilation error, then click on "discover m2e connectors". You need to install all four Tycho plugins.

Mylyn: http://download.eclipse.org/mylyn/releases/3.23 <br/>
JGit: http://download.eclipse.org/egit/updates-nightly <br/>

The Common Refactoring Framework that the current tool uses requires Eclipse SDK, Eclipse SDK Tests, and Eclipse testing framework. Input
"<b>The Eclipse Project Updates - http://download.eclipse.org/eclipse/updates/4.9 </b>" in the field of "Work with" after you clicked on the "Install New Software..." menu option under "Help" in Eclipse, then please check and install the three software mentioned above.

## Limitations

Our tool may miss log levels when log levels are passed via parameters due to the current lack of data flow analysis [issue 47](https://github.com/ponder-lab/Logging-Level-Evolution-Plugin/issues/47).

## Further information

Please see [wiki](https://github.com/ponder-lab/Logging-Level-Evolution-Plugin/wiki).
