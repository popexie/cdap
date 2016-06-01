# -*- coding: utf-8 -*-

# Copyright © 2014-2016 Cask Data, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

#
# CDAP documentation build configuration file
#
# This file is execfile()d with the current directory set to its
# containing dir.
#
# Note that not all possible configuration values are present in this
# autogenerated file.
#
# All configuration values have a default; values that are commented out
# serve to show the default.

# Component versions used in replacements:

cask_tracker_version = '0.1.0-SNAPSHOT'

cdap_apps_version = '0.7.0-SNAPSHOT'
cdap_apps_compatibile_version = 'release/cdap-3.4-compatible'

node_js_min_version = 'beginning with v0.10.36'
node_js_max_version = 'v4.4.0'

import sys
import os
import os.path
import subprocess
from datetime import datetime

def get_sdk_version():
    # Sets the Build Version
    version = None
    short_version = None
    full_version = None
    version_tuple = None
    try:
# Python 2.7 commands
#         grep_version_cmd = "grep '<version>' ../../../pom.xml | awk 'NR==1;START{print $1}'"
#         print "grep_version_cmd: %s" % grep_version_cmd
#         full_version_temp = subprocess.check_output(grep_version_cmd, shell=True)
# Python 2.6 commands
        p1 = subprocess.Popen(['grep' , '<version>', '../../../pom.xml' ], stdout=subprocess.PIPE)
        p2 = subprocess.Popen(['awk', 'NR==1;START{print $1}'], stdin=p1.stdout, stdout=subprocess.PIPE)
        full_version_temp = p2.communicate()[0]
# Python 2.6 command end
        full_version = full_version_temp.strip().replace('<version>', '').replace('</version>', '')
        version = full_version.replace('-SNAPSHOT', '')
        short_version = "%s.%s" % tuple(version.split('.')[0:2])
       
        v = full_version.replace('-', '.').split('.')
        if len(v) > 3:
            s = "%s-" % v[3]
            v = v[0:3]
            v.append(s)
            v.append(s)
        else:
            v.append('')
            v.append('')
        version_tuple = tuple(v)      
    except:
        print "Unexpected error: %s" % sys.exc_info()[0]
        pass
    return version, short_version, full_version, version_tuple

def print_sdk_version():
    version, short_version, full_version = get_sdk_version()
    if version == full_version:
        print "SDK Version: %s" % version
    elif version and full_version:
        print "SDK Version: %s (%s)" % (version, full_version)
        print "Version tuple: %s" % (version_tuple)
    else:
        print "Could not get version (%s), full version (%s) from grep" % (version, full_version)

def get_git_hash_timestamp():
    # Gets the Git commit information
    git_hash = None
    git_timestamp = None
    try:
        p1 = subprocess.Popen(['git' , 'rev-parse', 'HEAD' ], stdout=subprocess.PIPE)
        git_hash = p1.communicate()[0].strip()
        p2 = subprocess.Popen(['git', 'show', '-s', '--format=%ci', git_hash], stdout=subprocess.PIPE)
        git_timestamp = p2.communicate()[0].strip()
    except:
        print "Unexpected error: %s" % sys.exc_info()[0]
        pass
    return git_hash, git_timestamp

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#sys.path.insert(0, os.path.abspath('.'))

# -- General configuration ------------------------------------------------

# TO-DO: this is temp fix, as this is also specified in the build scripts
target = 'target'

# If your documentation needs a minimal Sphinx version, state it here.
needs_sphinx = '1.3'

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx.ext.ifconfig',
    'sphinx.ext.intersphinx',
    'sphinx.ext.extlinks',
    'tabbed-parsed-literal',
    'tabbed-block',
]

_intersphinx_mapping = "../../%%s/%s/html/objects.inv" % target

# The Inter-Sphinx mapping keys must be alpha-numeric only
intersphinx_mapping = {
  'introduction': ('../../introduction/',      os.path.abspath(_intersphinx_mapping % 'introduction')),
  'developers':   ('../../developers-manual/', os.path.abspath(_intersphinx_mapping % 'developers-manual')),
  'cdapapps':     ('../../cdap-apps',          os.path.abspath(_intersphinx_mapping % 'cdap-apps')),
  'admin':        ('../../admin-manual/',      os.path.abspath(_intersphinx_mapping % 'admin-manual')),
  'integrations': ('../../integrations/',      os.path.abspath(_intersphinx_mapping % 'integrations')),
  'examples':     ('../../examples-manual',    os.path.abspath(_intersphinx_mapping % 'examples-manual')),
  'reference':    ('../../reference-manual',   os.path.abspath(_intersphinx_mapping % 'reference-manual')),
  'faqs':         ('../../faqs',               os.path.abspath(_intersphinx_mapping % 'faqs')),
}

extlinks = {
    'cdap-ui-apps': ('http://localhost:9999/ns/default/apps/%s', None),
    'cdap-ui-apps-programs': ('http://localhost:9999/ns/default/apps/%s/overview/programs', None),
    'cdap-ui-datasets': ('http://localhost:9999/ns/default/datasets/%s', None),
    'cdap-ui-datasets-explore': ('http://localhost:9999/ns/default/datasets/%s/overview/explore', None),
}

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates', '../../_common/_templates']

# The suffix of source filenames.
source_suffix = '.rst'
try:
    from recommonmark.parser import CommonMarkParser
    source_parsers = {'.md': CommonMarkParser}
    source_suffix = ['.rst', '.md']
    print "Imported CommonMarkParser from recommonmark; can process Markdown files."
except ImportError:
    print "Unable to import CommonMarkParser from recommonmark; can't process Markdown files."

# The encoding of source files.
#source_encoding = 'utf-8-sig'

# The master toctree document.
master_doc = 'table-of-contents'

# General information about the project.
project = u'Cask Data Application Platform'
current_year = datetime.now().year
copyright = u'2014-%s Cask Data, Inc.' % current_year

# The version info for the project you're documenting, acts as replacement for
# |version| and |release|, also used in various other places throughout the
# built documents.
#
# The X.Y.Z version
# The X.Y short-version
# The full version, including alpha/beta/rc tags, or release version.
version, short_version, release, version_tuple = get_sdk_version()
git_hash, git_timestamp = get_git_hash_timestamp()

# The language for content autogenerated by Sphinx. Refer to documentation
# for a list of supported languages.
language = 'en_CDAP'
locale_dirs = ['_locale/', '../../_common/_locale']

# A string of reStructuredText that will be included at the end of every source file that
# is read. This is the right place to add substitutions that should be available in every
# file.
rst_epilog = """
.. role:: gp
.. |$| replace:: :gp:`$`

.. role:: gp
.. |#| replace:: :gp:`#`

.. role:: gp
.. |>| replace:: :gp:`>`

.. role:: gp
.. |cdap >| replace:: :gp:`cdap >`

.. |http:| replace:: http:
.. |https:| replace:: https:

.. |(TM)| unicode:: U+2122 .. trademark sign
   :ltrim:

.. |(R)| unicode:: U+00AE .. registered trademark sign
   :ltrim:

.. |--| unicode:: U+2013   .. en dash
.. |---| unicode:: U+2014  .. em dash, trimming surrounding whitespace
   :trim:
  
.. |non-breaking-space| unicode:: U+00A0 .. non-breaking space
"""

if node_js_min_version:
    rst_epilog += """
.. |node-js-min-version| replace:: %(node_js_min_version)s

.. |node-js-max-version| replace:: %(node_js_max_version)s

""" % {'node_js_min_version': node_js_min_version,
       'node_js_max_version': node_js_max_version,
      }

if version:
    rst_epilog += """
.. |bold-version| replace:: **%(version)s**

.. |italic-version| replace:: *%(version)s*

.. |literal-version| replace:: ``%(version)s``
""" % {'version': version}

if short_version:
    previous_short_version = float(short_version) -0.1
    rst_epilog += """
.. |short-version| replace:: %(short_version)s
.. |bold-short-version| replace:: **%(short_version)s**
.. |literal-short-version| replace:: ``%(short_version)s``
.. |previous-short-version| replace:: %(previous_short_version)s
.. |bold-previous-short-version| replace:: **%(previous_short_version)s**
.. |literal-previous-short-version| replace:: ``%(previous_short_version)s``

""" % {'short_version': short_version, 'previous_short_version': previous_short_version}

if version_tuple:
    rst_epilog += """
.. |version-major| replace:: %s
.. |version-minor| replace:: %s
.. |version-fix| replace:: %s
.. |version-suffix-batch| replace:: %sbatch
.. |version-suffix-realtime| replace:: %srealtime
""" % version_tuple

    rst_epilog += """
.. |literal-version-major| replace:: ``%s``
.. |literal-version-minor| replace:: ``%s``
.. |literal-version-fix| replace:: ``%s``
.. |literal-version-suffix-batch| replace:: ``%sbatch``
.. |literal-version-suffix-realtime| replace:: ``%srealtime``
""" % version_tuple

if release:
    rst_epilog += """
.. |literal-release| replace:: ``%(release)s``
""" % {'release': release}

if current_year:
    rst_epilog += """
.. |current_year| replace:: %(current_year)s
""" % {'current_year': current_year}

if copyright:
    rst_epilog += """
.. |copyright| replace:: %(copyright)s
""" % {'copyright': copyright}

if cdap_apps_version:
    rst_epilog += """
.. |cdap-apps-version| replace:: %(cdap-apps-version)s
.. |literal-cdap-apps-version| replace:: ``%(cdap-apps-version)s``
.. |cdap-apps-compatibile-version| replace:: %(cdap-apps-compatibile-version)s

""" % {'cdap-apps-version': cdap_apps_version, 'cdap-apps-compatibile-version': cdap_apps_compatibile_version}

if cask_tracker_version:
    rst_epilog += """
.. |cask-tracker-version| replace:: %(cask-tracker-version)s
.. |cask-tracker-version-jar| replace:: tracker-%(cask-tracker-version)s.jar
.. |literal-cask-tracker-version| replace:: ``%(cask-tracker-version)s``
.. |literal-cask-tracker-version-jar| replace:: ``tracker-%(cask-tracker-version)s.jar``

""" % {'cask-tracker-version': cask_tracker_version}

# There are two options for replacing |today|: either, you set today to some
# non-false value, then it is used:
#today = ''
# Else, today_fmt is used as the format for a strftime call.
#today_fmt = '%B %d, %Y'

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
exclude_patterns = ['_examples', '_includes']

# The reST default role (used for this markup: `text`) to use for all
# documents.
#default_role = None

# If true, '()' will be appended to :func: etc. cross-reference text.
#add_function_parentheses = True

# If true, the current module name will be prepended to all description
# unit titles (such as .. function::).
#add_module_names = True

# If true, sectionauthor and moduleauthor directives will be shown in the
# output. They are ignored by default.
#show_authors = False

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'

# The default language to highlight source code in.
highlight_language = 'java'

# A list of ignored prefixes for module index sorting.
#modindex_common_prefix = []

# If true, keep warnings as "system message" paragraphs in the built documents.
#keep_warnings = False

# -- Options for HTML output ----------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#html_theme = 'default'
#html_theme = 'nature'
#html_style = 'style.css'
html_theme = 'cdap'

# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
#
# html_theme_options = {"showtoc_include_showtocs":"false"}
# manuals and manual_titles are lists of the manuals in the doc set
#
# json_versions_js points to the JSON file on the webservers
# versions_data is used to generate the JSONP file at http://docs.cask.co/cdap/json-versions.js
# This is generated by a script in the documentation repo that is used to sync the webservers.
#
# manual_list is an ordered list of the manuals
# Fields: directory, manual name, icon
# icon: "" for none, "new-icon" for the ico_new.png
manuals_list = [
    ['introduction',       'Introduction to CDAP',            '',],
    ['developers-manual', u'Developers’ Manual',              '',],
    ['cdap-apps',          'CDAP Applications',               '',],
    ['admin-manual',       'Administration Manual',           '',],
    ['integrations',       'Integrations',                    '',],
    ['examples-manual',    'Examples, Guides, and Tutorials', '',],
    ['reference-manual',   'Reference Manual',                '',],
    ['faqs',               'FAQs',                            '',],
]

manual_intersphinx_mapping = {
  'introduction': 'introduction',
  'developers-manual': 'developers',
  'cdap-apps': 'cdapapps',
  'admin-manual': 'admin',
  'integrations': 'integrations',
  'examples-manual': 'examples',
  'reference-manual': 'reference',
  'faqs': 'faqs',
}

manuals_dict = {}
manual_titles_list = []
manual_dirs_list  = []
manual_icons_list = []
for m in manuals_list:
    manuals_dict[m[0]]= m[1]
    manual_dirs_list.append(m[0])
    manual_titles_list.append(m[1])
    manual_icons_list.append(m[2])

html_theme_options = {
  'docs_url': 'http://docs.cask.co/cdap',
  'language': 'en',
  'manual': '',
  'manuals': manual_dirs_list,
  'manual_titles': manual_titles_list,
  'manual_icons': manual_icons_list,
  'meta_git':
    { 'git_hash': git_hash,
      'git_timestamp': git_timestamp,
      'git_release': release,
    },
  'release': release,
  'version': version,
  'json_versions_js': 'http://docs.cask.co/cdap/json-versions.js',
}

def get_manuals():
    return html_theme_options['manuals']

def get_manual_titles():
    return html_theme_options['manual_titles']

def get_manual_titles_bash():
    PREFIX = 'declare -a MANUAL_TITLES=('
    SUFFIX = ');'
    manual_titles = PREFIX
    for title in html_theme_options['manual_titles']:
        manual_titles += "'%s'" % title
    manual_titles += SUFFIX
    return manual_titles

# Add Google Tag Manager Code, or over-ride on the command line with
# -A html_google_tag_manager_code=GTM-XXXXXX
html_google_tag_manager_code = ''

# Add any paths that contain custom themes here, relative to this directory.
html_theme_path = ['_themes','../../_common/_themes']

# The name for this set of Sphinx documents.  If None, it defaults to
# "<project> v<release> documentation".
#html_title = None

# A shorter title for the navigation bar.  Default is the same as html_title.
html_short_title = u"CDAP Documentation v%s" % version

# A shorter title for the sidebar section, preceding the words "Table of Contents".
html_short_title_toc = u'CDAP Documentation'

# The name of an image file (relative to this directory) to place at the top
# of the sidebar.
#html_logo = None

# The name of an image file (within the static path) to use as favicon of the
# docs.  This file should be a Windows icon file (.ico) being 16x16 or 32x32
# pixels large.
# html_favicon = '_static/favicon.ico'
html_favicon = '../../_common/_static/favicon.ico'

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['../../_common/_static']

# Add any extra paths that contain custom files (such as robots.txt or
# .htaccess) here, relative to this directory. These files are copied
# directly to the root of the documentation.
#html_extra_path = []

# If not '', a 'Last updated on:' timestamp is inserted at every page bottom,
# using the given strftime format.
#html_last_updated_fmt = '%b %d, %Y'

# If true, SmartyPants will be used to convert quotes and dashes to
# typographically correct entities.
#html_use_smartypants = True

# Custom sidebar templates, maps document names to template names.
html_sidebars = {'**': [
    'manuals.html',
    'globaltoc.html',
    'relations.html',
    'downloads.html',
    'searchbox.html',
    'casksites.html',
     ],}

# Additional templates that should be rendered to pages, maps page names to
# template names.
#html_additional_pages = {}

# If false, no module index is generated.
#html_domain_indices = True

# If false, no index is generated.
#html_use_index = True

# If true, the index is split into individual pages for each letter.
#html_split_index = False

# If true, links to the reST sources are added to the pages.
html_show_sourcelink = False
html_copy_source = False

# If true, "Created using Sphinx" is shown in the HTML footer. Default is True.
html_show_sphinx = False

# If true, "(C) Copyright ..." is shown in the HTML footer. Default is True.
#html_show_copyright = True

# If true, an OpenSearch description file will be output, and all pages will
# contain a <link> tag referring to it.  The value of this option must be the
# base URL from which the finished HTML is served.
#html_use_opensearch = ''

# This is the file name suffix for HTML files (e.g. ".xhtml").
#html_file_suffix = None

# Output file base name for HTML help builder.
htmlhelp_basename = 'CDAPdoc'

# This context needs to be created in each child conf.py. At a minimum, it needs to be
# html_context = {"html_short_title_toc":html_short_title_toc}
# This is because it needs to be set as the last item.
html_context = {'html_short_title_toc': html_short_title_toc}

# -- Options for LaTeX output ---------------------------------------------

latex_elements = {
# The paper size ('letterpaper' or 'a4paper').
#'papersize': 'letterpaper',

# The font size ('10pt', '11pt' or '12pt').
#'pointsize': '10pt',

# Additional stuff for the LaTeX preamble.
#'preamble': '',
}

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title,
#  author, documentclass [howto, manual, or own class]).
latex_documents = [
  ('index', 'CDAP.tex', u'CDAP Documentation',
   u'Cask Data, Inc.', 'manual'),
]

# The name of an image file (relative to this directory) to place at the top of
# the title page.
#latex_logo = None

# For "manual" documents, if this is true, then toplevel headings are parts,
# not chapters.
#latex_use_parts = False

# If true, show page references after internal links.
#latex_show_pagerefs = False

# If true, show URL addresses after external links.
#latex_show_urls = False

# Documents to append as an appendix to all manuals.
#latex_appendices = []

# If false, no module index is generated.
#latex_domain_indices = True


# -- Options for manual page output ---------------------------------------

# One entry per manual page. List of tuples
# (source start file, name, description, authors, manual section).
man_pages = [
    ('index', 'cdap', u'CDAP Documentation',
     [u'Cask Data, Inc.'], 1)
]

# If true, show URL addresses after external links.
#man_show_urls = False


# -- Options for Texinfo output -------------------------------------------

# Grouping the document tree into Texinfo files. List of tuples
# (source start file, target name, title, author,
#  dir menu entry, description, category)
texinfo_documents = [
  ('index', 'CDAP', u'CDAP Documentation',
   u'Cask Data, Inc.', 'CDAP', 'Cask Data Application Platform',
   'Miscellaneous'),
]

# Documents to append as an appendix to all manuals.
#texinfo_appendices = []

# If false, no module index is generated.
#texinfo_domain_indices = True

# How to display URL addresses: 'footnote', 'no', or 'inline'.
#texinfo_show_urls = 'footnote'

# If true, do not generate a @detailmenu in the "Top" node's menu.
#texinfo_no_detailmenu = False


# Example configuration for intersphinx: refer to the Python standard library.
#intersphinx_mapping = {'http://docs.python.org/': None}

# -- Options for PDF output --------------------------------------------------

# Grouping the document tree into PDF files. List of tuples
# (source start file, target name, title, author, options).
#
# If there is more than one author, separate them with \\.
# For example: r'Guido van Rossum\\Fred L. Drake, Jr., editor'
#
# The options element is a dictionary that lets you override
# this config per-document.
# For example,
# ('index', u'MyProject', u'My Project', u'Author Name',
#  dict(pdf_compressed = True))
# would mean that specific document would be compressed
# regardless of the global pdf_compressed setting.

pdf_documents = [
    ('index', u'CDAP', u'Cask Data Application Platform', u'Cask Data, Inc.'),
]

# A comma-separated list of custom stylesheets. Example:
#pdf_stylesheets = ['sphinx','kerning','a4']
pdf_stylesheets = ['pdf-stylesheet']

# A list of folders to search for stylesheets. Example:
pdf_style_path = ['.', '_templates', '_styles']

# Create a compressed PDF
# Use True/False or 1/0
# Example: compressed=True
#pdf_compressed = False

# A colon-separated list of folders to search for fonts. Example:
# pdf_font_path = ['/usr/share/fonts', '/usr/share/texmf-dist/fonts/']
pdf_font_path = ['/Library/fonts', '~/Library/Fonts']

# Language to be used for hyphenation support
pdf_language = 'en_US'

# Mode for literal blocks wider than the frame. Can be
# overflow, shrink or truncate
pdf_fit_mode = 'shrink'

# Section level that forces a break page.
# For example: 1 means top-level sections start in a new page
# 0 means disabled
pdf_break_level = 1

# When a section starts in a new page, force it to be 'even', 'odd',
# or just use 'any'
pdf_breakside = 'any'

# Insert footnotes where they are defined instead of
# at the end.
#pdf_inline_footnotes = True

# verbosity level. 0 1 or 2
#pdf_verbosity = 0

# If false, no index is generated.
pdf_use_index = False

# If false, no modindex is generated.
pdf_use_modindex = False

# If false, no coverpage is generated.
pdf_use_coverpage = False

# Name of the cover page template to use
#pdf_cover_template = 'sphinxcover.tmpl'

# Documents to append as an appendix to all manuals.
#pdf_appendices = []

# Enable experimental feature to split table cells. Use it
# if you get "DelayedTable too big" errors
#pdf_splittables = False

# Set the default DPI for images
#pdf_default_dpi = 72

# Enable rst2pdf extension modules (default is only vectorpdf)
# you need vectorpdf if you want to use sphinx's graphviz support
#pdf_extensions = ['vectorpdf']

# Page template name for "regular" pages
#pdf_page_template = 'cutePage'

# Show Table Of Contents at the beginning?
#pdf_use_toc = True

# How many levels deep should the table of contents be?
pdf_toc_depth = 9999

# Add section number to section references
pdf_use_numbered_links = False

# Background images fitting mode
pdf_fit_background_mode = 'scale'

# -- Options for Manuals --------------------------------------------------

def set_conf_for_manual():
    m = os.path.basename(os.path.normpath(os.path.join(os.getcwd(), '..')))
    print "set_conf_for_manual: %s" % m

    html_theme_options['manual'] = m
    html_short_title_toc = manuals_dict[m]
    html_short_title = u'CDAP %s' % html_short_title_toc
    html_context = {'html_short_title_toc': html_short_title_toc}

    # Remove this guide from the mapping as it will fail as it has been deleted by clean
    intersphinx_mapping.pop(manual_intersphinx_mapping[m], None)

    return html_short_title_toc, html_short_title, html_context

# -- Handle Markdown files --------------------------------------------------

def source_read_handler(app, docname, source):
    doc_path = app.env.doc2path(docname)
    if doc_path.endswith(".md"):
        # Cache the self.env.config.rst_epilog and rst_prolog
        if app.env.config.rst_epilog:
            app.env.config.rst_epilog_cache = app.env.config.rst_epilog
            app.env.config.rst_epilog = None
        if app.env.config.rst_prolog:
            app.env.config.rst_prolog_cache = app.env.config.rst_prolog
            app.env.config.rst_prolog = None
    else:
        if not app.env.config.rst_epilog and hasattr(app.env.config, 'rst_epilog_cache') and app.env.config.rst_epilog_cache:
            app.env.config.rst_epilog = app.env.config.rst_epilog_cache
        if not app.env.config.rst_prolog and hasattr(app.env.config, 'rst_prolog_cache') and app.env.config.rst_prolog_cache:
            app.env.config.rst_prolog = app.env.config.rst_prolog_cache

def setup(app):
    app.connect('source-read', source_read_handler)
