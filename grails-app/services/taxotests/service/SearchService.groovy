package taxotests.service

class SearchService {
    static transactional = false

    def taxonomyService

    //this is a workaround for http://jira.grails.org/browse/GPTAXONOMY-6
    public <T> Collection<Class<T>> findAllByExactTag(Class<T> objClass, Locale locale, String tag) {
        def taxon = taxonomyService.resolveTaxon(['by_locale', locale.toString(), tag])
        if (taxon != null) {
            taxonomyService.findObjectsByFamily(objClass, taxon)
        } else {
            Collections.EMPTY_LIST
        }
    }
}
