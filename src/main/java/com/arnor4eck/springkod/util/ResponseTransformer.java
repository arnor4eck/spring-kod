package com.arnor4eck.springkod.util;

import com.arnor4eck.springkod.entity.datasitory_file.ImageUrl;
import com.arnor4eck.springkod.repository.ImageUrlRepository;
import com.arnor4eck.springkod.util.key.KeyGenerator;
import com.arnor4eck.springkod.util.response.ml.*;
import com.arnor4eck.springkod.util.response.ml.to_frontend.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ResponseTransformer {

    private final ImageUrlRepository imageUrlRepository;

    private final KeyGenerator keyGenerator;

    public MlAnalyticsResponseWithUrls transformToFrontendResponse(long datasitoryId, MlAnalyticsResponse response) {
        GroupsWithUrls groupsWithUrls = transformGroups(datasitoryId, response.groups());

        return new MlAnalyticsResponseWithUrls(
                response.summary(),
                groupsWithUrls,
                response.roadmap()
        );
    }

    private GroupsWithUrls transformGroups(long datasitoryId, Groups groups) {
        if (groups == null) {
            return null;
        }

        return new GroupsWithUrls(
                transformAllObjects(datasitoryId, groups.allObjects()),
                transformReliableObjects(datasitoryId, groups.reliable()),
                transformLabelIssues(datasitoryId, groups.labelIssues()),
                transformDuplicateGroups(datasitoryId, groups.duplicates()),
                transformQualityIssues(datasitoryId, groups.qualityIssues())
        );
    }

    private List<AllObjectWithUrl> transformAllObjects(long datasitoryId, List<AllObject> allObjects) {
        List<AllObjectWithUrl> result = new LinkedList<>();

        for (AllObject obj : allObjects) {
            String url = getUrlByFileName(datasitoryId, obj.fileName());

            AllObjectWithUrl transformed = new AllObjectWithUrl(
                    obj.fileName(),
                    url,
                    obj.tags(),
                    obj.utilityScore(),
                    obj.entropy(),
                    obj.confidence(),
                    obj.labelScore(),
                    obj.outlierScore()
            );
            result.add(transformed);
        }

        return result;
    }

    private List<ReliableObjectWithUrl> transformReliableObjects(long datasitoryId, List<ReliableObject> reliable) {
        List<ReliableObjectWithUrl> result = new LinkedList<>();

        for (ReliableObject obj : reliable) {
            String url = getUrlByFileName(datasitoryId, obj.fileName());

            ReliableObjectWithUrl transformed = new ReliableObjectWithUrl(
                    obj.fileName(),
                    url,
                    obj.tags(),
                    obj.utilityScore()
            );
            result.add(transformed);
        }

        return result;
    }

    private List<LabelIssueWithUrl> transformLabelIssues(long datasitoryId, List<LabelIssue> labelIssues) {
        List<LabelIssueWithUrl> result = new LinkedList<>();

        for (LabelIssue issue : labelIssues) {
            String url = getUrlByFileName(datasitoryId, issue.fileName());

            LabelIssueWithUrl transformed = new LabelIssueWithUrl(
                    issue.fileName(),
                    url,
                    issue.oldLabel(),
                    issue.oldLabelName(),
                    issue.suggestedLabel(),
                    issue.suggestedLabelName()
            );
            result.add(transformed);
        }

        return result;
    }

    private List<DuplicateGroupWithUrl> transformDuplicateGroups(long datasitoryId, List<DuplicateGroup> duplicateGroups) {
        List<DuplicateGroupWithUrl> result = new LinkedList<>();

        for (DuplicateGroup group : duplicateGroups) {
            DuplicateGroupWithUrl transformed = new DuplicateGroupWithUrl(
                    group.groupId(),
                    transformDuplicateFile(datasitoryId, group.primary()),
                    transformDuplicateFiles(datasitoryId, group.copies())
            );
            result.add(transformed);
        }

        return result;
    }

    private DuplicateFileWithUrl transformDuplicateFile(long datasitoryId, DuplicateFile file) {
        if (file == null) {
            return null;
        }

        String url = getUrlByFileName(datasitoryId, file.fileName());

        return new DuplicateFileWithUrl(
                file.fileName(),
                url
        );
    }

    private List<DuplicateFileWithUrl> transformDuplicateFiles(long datasitoryId,List<DuplicateFile> files) {
        List<DuplicateFileWithUrl> result = new LinkedList<>();

        for (DuplicateFile file : files) {
            result.add(transformDuplicateFile(datasitoryId, file));
        }

        return result;
    }

    private List<QualityIssueWithUrl> transformQualityIssues(long datasitoryId, List<QualityIssue> qualityIssues) {
        List<QualityIssueWithUrl> result = new LinkedList<>();

        for (QualityIssue issue : qualityIssues) {
            String url = getUrlByFileName(datasitoryId, issue.fileName());

            QualityIssueWithUrl transformed = new QualityIssueWithUrl(
                    issue.fileName(),
                    url,
                    issue.tags(),
                    issue.qualityScore()
            );
            result.add(transformed);
        }

        return result;
    }

    private String getUrlByFileName(long datasitoryId, String fileName) {
        String fileId = keyGenerator.generateKey(fileName, datasitoryId);

        ImageUrl url = imageUrlRepository.findByDatasitoryFileFileId(fileId);

        return url.getUrl();
    }
}
