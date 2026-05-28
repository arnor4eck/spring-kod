import apiClient from './api';
import type { MlAnalyticsResponse, BrokenFile } from '../types/types';

export const analyticsAPI = {
    getAnalytics: async (datasetId: number): Promise<MlAnalyticsResponse> => {
        const response = await apiClient.get(`/v1/analitics/${datasetId}`);
        console.log(response);
        return response.data;
    },
    
    checkBrokenFiles: async (datasetId: number): Promise<BrokenFile[]> => {
    const response = await apiClient.get(`/v1/files/images/beat/${datasetId}`);
    const beatFiles: { fileName: string }[] = response.data;
    
    return beatFiles.map(beatFile => ({
        name: beatFile.fileName,
        reason: "Файл повреждён или имеет неверный формат"
    }));
    },
};

// import apiClient from './api';
// import type { MlAnalyticsResponse, BrokenFile } from '../types/types';

// export const analyticsAPI = {
//     getAnalytics: async (datasetId: number): Promise<MlAnalyticsResponse> => {
//         console.log('Запрос аналитики для датасета ID:', datasetId);
        
//         // TODO: Раскомментировать, когда бэкенд будет готов
//         /*
//         try {
//             const response = await apiClient.get(`/v1/analytics/${datasetId}`);
//             return response.data;
//         } catch (error) {
//             console.error('Ошибка при запросе аналитики:', error);
//             throw error;
//         }
//         */
        
//         console.log('Используются моковые данные для макета');
        
//         // Моковые данные на основе train.csv
//         return {
//             summary: {
//                 readiness: 75,
//                 n_total: 10,
//                 n_classes: 5,
//                 classes: [
//                     {
//                         class_idx: 0,
//                         name: "Badminton",
//                         count: 1,
//                         percentage: 10.0,
//                         deficit: 0.5
//                     },
//                     {
//                         class_idx: 1,
//                         name: "Soccer",
//                         count: 2,
//                         percentage: 20.0,
//                         deficit: 0.0
//                     },
//                     {
//                         class_idx: 2,
//                         name: "Swimming",
//                         count: 1,
//                         percentage: 10.0,
//                         deficit: 0.5
//                     },
//                     {
//                         class_idx: 3,
//                         name: "Tennis",
//                         count: 3,
//                         percentage: 30.0,
//                         deficit: 0.0
//                     },
//                     {
//                         class_idx: 4,
//                         name: "Wrestling",
//                         count: 3,
//                         percentage: 30.0,
//                         deficit: 0.0
//                     }
//                 ]
//             },
//             groups: {
//                 all_objects: [
//                     {
//                         file_name: "00a87b6bb3.jpg",
//                         url: "https://picsum.photos/id/1/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.52,
//                         entropy: 0.00000159,
//                         confidence: 0.99999988,
//                         label_score: 0.99999988,
//                         outlier_score: 0.41
//                     },
//                     {
//                         file_name: "00bca71f3b.jpg",
//                         url: "https://picsum.photos/id/2/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.14,
//                         entropy: 0.00000000001,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.38
//                     },
//                     {
//                         file_name: "00bfe31734.jpg",
//                         url: "https://picsum.photos/id/3/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.87,
//                         entropy: 0.00000000002,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.31
//                     },
//                     {
//                         file_name: "00d4ec9692.jpg",
//                         url: "https://picsum.photos/id/4/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.0,
//                         entropy: 0.000000000001,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.39
//                     },
//                     {
//                         file_name: "00db6efcb9.jpg",
//                         url: "https://picsum.photos/id/5/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.41,
//                         entropy: 0.00000005,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.36
//                     },
//                     {
//                         file_name: "00edf6dae0.jpg",
//                         url: "https://picsum.photos/id/6/100/100",
//                         tags: ["deficit_class", "confident_prediction", "reliable"],
//                         utility_score: 0.90,
//                         entropy: 0.00000000001,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.37
//                     },
//                     {
//                         file_name: "0a0ac9718b.jpg",
//                         url: "https://picsum.photos/id/7/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.53,
//                         entropy: 0.00000000001,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.34
//                     },
//                     {
//                         file_name: "0a1a257824.jpg",
//                         url: "https://picsum.photos/id/8/100/100",
//                         tags: ["deficit_class", "confident_prediction", "reliable"],
//                         utility_score: 1.0,
//                         entropy: 0.0000000000003,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.36
//                     },
//                     {
//                         file_name: "0a1ff1be27.jpg",
//                         url: "https://picsum.photos/id/9/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.23,
//                         entropy: 0.0000000000004,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.37
//                     },
//                     {
//                         file_name: "0a3f4b8a40.jpg",
//                         url: "https://picsum.photos/id/10/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.50,
//                         entropy: 0.000000000003,
//                         confidence: 1.0,
//                         label_score: 1.0,
//                         outlier_score: 0.35
//                     }
//                 ],
//                 reliable: [
//                     {
//                         file_name: "0a1a257824.jpg",
//                         url: "https://picsum.photos/id/8/100/100",
//                         tags: ["deficit_class", "confident_prediction", "reliable"],
//                         utility_score: 1.0
//                     },
//                     {
//                         file_name: "00edf6dae0.jpg",
//                         url: "https://picsum.photos/id/6/100/100",
//                         tags: ["deficit_class", "confident_prediction", "reliable"],
//                         utility_score: 0.90
//                     },
//                     {
//                         file_name: "00bfe31734.jpg",
//                         url: "https://picsum.photos/id/3/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.87
//                     },
//                     {
//                         file_name: "0a0ac9718b.jpg",
//                         url: "https://picsum.photos/id/7/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.53
//                     },
//                     {
//                         file_name: "00a87b6bb3.jpg",
//                         url: "https://picsum.photos/id/1/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.52
//                     },
//                     {
//                         file_name: "0a3f4b8a40.jpg",
//                         url: "https://picsum.photos/id/10/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.50
//                     },
//                     {
//                         file_name: "00db6efcb9.jpg",
//                         url: "https://picsum.photos/id/5/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.41
//                     },
//                     {
//                         file_name: "0a1ff1be27.jpg",
//                         url: "https://picsum.photos/id/9/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.23
//                     },
//                     {
//                         file_name: "00bca71f3b.jpg",
//                         url: "https://picsum.photos/id/2/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.14
//                     },
//                     {
//                         file_name: "00d4ec9692.jpg",
//                         url: "https://picsum.photos/id/4/100/100",
//                         tags: ["confident_prediction", "reliable"],
//                         utility_score: 0.0
//                     }
//                 ],
//                 label_issues: [
//                     {
//                         file_name: "00a87b6bb3.jpg",
//                         url: "https://picsum.photos/id/1/100/100",
//                         old_label: 3,
//                         old_label_name: "Tennis",
//                         suggested_label: 1,
//                         suggested_label_name: "Soccer"
//                     },
//                     {
//                         file_name: "0a3f4b8a40.jpg",
//                         url: "https://picsum.photos/id/10/100/100",
//                         old_label: 3,
//                         old_label_name: "Tennis",
//                         suggested_label: 4,
//                         suggested_label_name: "Wrestling"
//                     }
//                 ],
//                 duplicates: [
//                     {
//                         group_id: 1,
//                         primary: {
//                             file_name: "00bfe31734.jpg",
//                             url: "https://picsum.photos/id/3/100/100"
//                         },
//                         copies: [
//                             {
//                                 file_name: "00db6efcb9.jpg",
//                                 url: "https://picsum.photos/id/5/100/100"
//                             }
//                         ]
//                     },
//                     {
//                         group_id: 2,
//                         primary: {
//                             file_name: "0a1a257824.jpg",
//                             url: "https://picsum.photos/id/8/100/100"
//                         },
//                         copies: [
//                             {
//                                 file_name: "00edf6dae0.jpg",
//                                 url: "https://picsum.photos/id/6/100/100"
//                             }
//                         ]
//                     }
//                 ],
//                 quality_issues: [
//                     {
//                         file_name: "00d4ec9692.jpg",
//                         url: "https://picsum.photos/id/4/100/100",
//                         tags: ["blurry", "low_resolution"],
//                         quality_score: 0.35
//                     },
//                     {
//                         file_name: "0a1ff1be27.jpg",
//                         url: "https://picsum.photos/id/9/100/100",
//                         tags: ["overexposed"],
//                         quality_score: 0.42
//                     }
//                 ]
//             },
//             roadmap: [
//                 {
//                     id: 1,
//                     action: "Add 1 examples of class 'Badminton' (deficit: 50%)"
//                 },
//                 {
//                     id: 2,
//                     action: "Add 1 examples of class 'Swimming' (deficit: 50%)"
//                 },
//                 {
//                     id: 3,
//                     action: "Review 2 controversial labels (Tennis → Soccer, Tennis → Wrestling)"
//                 },
//                 {
//                     id: 4,
//                     action: "Remove 2 duplicate images"
//                 },
//                 {
//                     id: 5,
//                     action: "Replace 2 low quality images (blurry, overexposed)"
//                 }
//             ]
//         };
//     },
    
//     checkBrokenFiles: async (files: File[]): Promise<BrokenFile[]> => {
//         const formData = new FormData();
//         files.forEach(file => {
//             formData.append('files', file);
//         });
//         const response = await apiClient.post('/files/check-broken', formData, {
//             headers: { 'Content-Type': 'multipart/form-data' }
//         });
//         return response.data;
//     },
// };