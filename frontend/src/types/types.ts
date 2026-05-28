export interface User {
    id: number;
    username: string;
    email: string;
    createdAt: string;
}

export interface CreateUserRequest {
    username: string;
    email: string;
    password: string;
}

export interface AuthenticationRequest {
    email: string;
    password: string;
}

export interface AuthenticationResponse {
    token: string;
}

export interface Creator {
    id: number;
    email: string;
    username: string;
    createdAt: string;
}

export interface Dataset {
    id: number;
    name: string;
    description: string;
    type: string;
    createdAt: string;
    updatedAt: string;
    creator: Creator;
}

export interface CreateDatasetRequest {
    name: string;
    description?: string;
    visibility: 'PUBLIC' | 'PRIVATE';
    imageUrls: string[];
    labelsFile: File;
    probabilitiesFile?: File;
    metadataFile?: File;
}

export interface FileInfoResponse {
    id: number;
    name: string;
    originalName: string;
    contentType: string;
    size: number;
    fileType: 'IMAGE' | 'PROBABILITY' | 'METADATA' | 'MARKUP';
    createdAt: string;
}

export interface MlAnalyticsResponse {
    summary: Summary;
    groups: Groups;
    roadmap: RoadmapItem[];
}

export interface Summary {
    readiness: number;
    n_total: number;
    n_classes: number;
    classes: ClassInfo[];
}

export interface ClassInfo {
    class_idx: number;
    name: string;
    count: number;
    percentage: number;
    deficit: number;
}

export interface Groups {
    all_objects: AllObject[];
    reliable: ReliableObject[];
    label_issues: LabelIssue[];
    duplicates: DuplicateGroup[];
    quality_issues: QualityIssue[];
}

export interface AllObject {
    file_name: string;
    url: string;
    tags: string[];
    utility_score: number;
    entropy: number;
    confidence: number;
    label_score: number;
    outlier_score: number;
}

export interface ReliableObject {
    file_name: string;
    url: string;
    tags: string[];
    utility_score: number;
}

export interface LabelIssue {
    file_name: string;
    url: string;
    old_label: number;
    old_label_name: string;
    suggested_label: number;
    suggested_label_name: string;
}

export interface DuplicateGroup {
    group_id: number;
    primary: DuplicateFile;
    copies: DuplicateFile[];
}

export interface DuplicateFile {
    file_name: string;
    url: string;
}

export interface QualityIssue {
    file_name: string;
    url: string;
    tags: string[];
    quality_score: number;
}

export interface RoadmapItem {
    id: number;
    action: string;
}

export interface BrokenFile {
    name: string;
    reason: string;
}

export interface FileToDelete {
    fileName: string;
}

export interface DeleteMarkupLineRequest {
    filesToDelete: FileToDelete[];
}

export interface FileToUpdate {
    fileName: string;
    newLabel: string;
}

export interface UpdateMarkupLineRequest {
    filesToUpdate: FileToUpdate[];
}