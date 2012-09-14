package taxotests.service

class SearchService {
    static transactional = false

    def tagTranslationService

    //this is a workaround for http://jira.grails.org/browse/GPTAXONOMY-6
    public <T> Collection<T>findAllByTag(Class<T> objClass, Locale locale, String tag) {
        def taxon = tagTranslationService.resolveTaxon(['by_locale', locale.toString(), tag])
        if (taxon != null) {
            tagTranslationService.findObjectsByFamily(objClass, taxon)
        } else {
            Collections.EMPTY_LIST
        }
    }

    public <T> Collection<T> findAllByTags(Class<T> objClass, Locale locale, List<String> tags) {
        tagTranslationService.findAllObjectsByTagList(objClass,locale,tags)
    }
}
