package org.nuxeo.ecm.directory.multi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nuxeo.ecm.directory.AbstractReference;
import org.nuxeo.ecm.directory.Directory;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.Reference;

public class MultiReference extends AbstractReference {
    
    final MultiDirectory dir;
    
    final String fieldName;
    
    MultiReference(MultiDirectory dir, String fieldName) {
        this.dir = dir;
        this.fieldName = fieldName;      
    }

    public void addLinks(String sourceId, List<String> targetIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void addLinks(List<String> sourceIds, String targetId)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    protected interface Collector {
        List<String> collect(Reference dir) throws DirectoryException;
    }
    
    protected List<String> doCollect(Collector extractor) throws DirectoryException {
        Set<String> ids = new HashSet<String>();
        for(SourceDescriptor src:dir.getDescriptor().sources) {
            for (SubDirectoryDescriptor sub:src.subDirectories) {
                Directory dir = MultiDirectoryFactory.getDirectoryService().getDirectory(
                        sub.name);
                if (dir == null) {
                    continue;
                }
                Reference ref = dir.getReference(fieldName);
                if (ref == null) {
                    continue;
                }
                ids.addAll(extractor.collect(ref));
            }
        }
        List<String> x = new ArrayList<String>(ids.size());
        x.addAll(ids);
        return x;
    }
    
    public List<String> getSourceIdsForTarget(final String targetId)
            throws DirectoryException {
        return doCollect(new Collector() {
            public List<String> collect(Reference ref) throws DirectoryException {
                return ref.getSourceIdsForTarget(targetId);
            }       
        });
    }

    public List<String> getTargetIdsForSource(final String sourceId)
            throws DirectoryException {
        return doCollect(new Collector() {
            public List<String> collect(Reference ref) throws DirectoryException {
                return ref.getSourceIdsForTarget(sourceId);
            }       
        });
    }

    public void removeLinksForSource(String sourceId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void removeLinksForTarget(String targetId) throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void setSourceIdsForTarget(String targetId, List<String> sourceIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

    public void setTargetIdsForSource(String sourceId, List<String> targetIds)
            throws DirectoryException {
        throw new UnsupportedOperationException();
    }

}