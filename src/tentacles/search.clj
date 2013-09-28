(ns tentacles.search
  "Implements the Github Search API: http://developer.github.com/v3/search/"
  (:use [tentacles.core :only [api-call]]))

(defn search-issues
  "Find issues by state and keyword"
  [user repo state keyword & [options]]
  (let [results (api-call :get
                          "/legacy/issues/search/%s/%s/%s/%s"
            			  [user repo state keyword]
            			  options)]
    (or (:issues results)
        results)))

(defn search-repos
  "Find repositories by keyword. This is a legacy method and does not follow
   API v3 pagination rules. It will return up to 100 results per page and pages
   are fetched by passing the start-page parameter.
   Options are:
     start-page: a number. Default is first page.
     language: filter by language"
  [keyword & [options]]
  (let [results (api-call :get "/legacy/repos/search/%s" [keyword] options)]
    (or (:repositories results)
        results)))

(defn search3-repos
  "Finds repositories by keyword. This is totally experimental.
Parameters:

topic: what you're searching for

in: where to search. Possibilities: name,description,readme
For now, use a csv for in, though that's cheeseball.
No need to get fancy during Clojure Cup.

size: match size in kilobytes.
This is worthy of some thought. API examples include:
1000 - 1 MB exactly
>=30000 - at least 30 MB
<50 - smaller than 50 KB
50..120 - between 50 and 120 KB

forks: how many times has this repo been forked?
Same options as size, plus:
true - include forks (defaults to false)
only - only repositories that are forked (looks like it means
the actual forks instead of the original)

created:
When was the repository created?
pushed:
When was the repository last updated?
These dates are in YYYY-MM-DD format.
Use < for \"before\" and > for \"after\"
e.g.
created:<2011 - created before 2011
pushed:>=2013-03-06 - updated since March 6, 2013

@user/repo - specify user and repository
Specifying repo seems more than a little silly.

language: what language interests us?
e.g. javascript

stars: How many are we looking for?
API docs mention that
stars:10..20 \"Matches repositories 10 to 20 stars,
that are smaler than 1000 KB\"
Until I get further clarification, assume that's a typo.

sort:
stars, forks, or updated. If not provided, sort by best
match

order:
asc or desc. Defaults to desc

options is used in quite a few places. The main one I'm 
interested in at the moment is \"all_pages\" which, according
to tentacle's docs, should return a lazy seq of everything.
"
  [topic {:keys [in size forks created pushed language stars sort order]}
   & options]
  ;; This is pretty cheesy.
  ;; And mostly wrong. Really should be passing it into api-call as options
  ;; so it can handle the escaping.
  ;; Except that those options don't seem to look much like these at all.
  ;; TODO: This should almost definitely be more general
  (let [request-path "/search/repositories?q=%s" 
        request-path (if in
                       (str request-path "+in:" in)
                       request-path)

        ;; etc
        ;; Except...build that using reduce
        request-path (if sort
                       (str request-path "&sort=" sort)
                       request-path)
        request-path (if order
                       (str request-path "&order=" order)
                       request-path)
        ;; c.f. core/make-request
        ;; This is destructured as a :strs key named accept
        ;; According to github's specs, need to "provide a
        ;; custom media type in the Accept header:
        ;; application/vnd.github.preview
        ;; "
        ;; This is my first guess at what that actually means.
        options (into options {"accept" "application/vnd.github.preview"})]
    (let [results (api-call :get request-path [topic])]
      (or (:repositories results)
          results))))

(defn search-users
  "Find users by keyword."
  [keyword & [options]]
  (let [results (api-call :get "/legacy/user/search/%s" [keyword] options)]
    (or (:users results)
        results)))
